package team6230.koiupstream.tunable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import org.littletonrobotics.junction.networktables.LoggedNetworkNumber;
import org.littletonrobotics.junction.networktables.LoggedNetworkString;

/**
 * Universal manager for {@code @Tunable} annotations.
 * Automatically links class fields to AdvantageKit/NetworkTables via reflection
 * and handles live bidirectional syncing of values.
 */
public class TunableManager {
    /**
     * Global toggle for tuning mode. If set to false, registering and syncing
     * tunables will be bypassed to save processing time during competition.
     */
    public static boolean tuningModeEnabled = true;

    /**
     * Internal interface to unify different AdvantageKit LoggedNetwork types
     * (Number, Boolean, String) into a single standard for reflection operations.
     */
    private interface LoggedValue {
        /**
         * Retrieves the current value from NetworkTables/AdvantageKit.
         * * @return The current value from the network.
         */
        Object get();

        /**
         * Applies the fetched network value to the physical Java field via reflection.
         * * @param field The reflection {@link Field} being modified.
         * 
         * @param parent The object instance containing the field.
         * @throws IllegalAccessException If the field cannot be modified.
         */
        void setField(Field field, Object parent) throws IllegalAccessException;
    }

    /**
     * Represents a live connection between a specific Java field and its
     * corresponding
     * NetworkTables/AdvantageKit entry.
     */
    private static class TunableConnection {
        private final Field field;
        private final Object parent;
        private final LoggedValue networkValue;
        private Object lastValue; // Tracks the last value for this specific instance

        public TunableConnection(Field field, Object parent, LoggedValue networkValue) {
            this.field = field;
            this.parent = parent;
            this.networkValue = networkValue;

            try {
                this.lastValue = field.get(parent);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        boolean sync() {
            try {
                Object currentValue = networkValue.get();

                // Compare the current network value with this instance's last known value
                if (!Objects.equals(currentValue, lastValue)) {
                    networkValue.setField(field, parent);
                    lastValue = currentValue;
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Internal registry keeping track of all active TunableConnections, mapped by
     * the object instance.
     */
    private static final Map<Object, List<TunableConnection>> registry = new HashMap<>();

    /**
     * Scans an object for {@code @Tunable} fields and registers them to
     * NetworkTables. Supports double, int, boolean, and String types.
     * * @param obj The class instance to scan (usually 'this' inside a subsystem
     * constructor).
     * 
     * @param path The NetworkTables root path (e.g., "/Tuning/Intake/").
     */
    public static void register(Object obj, String path) {
        if (!tuningModeEnabled)
            return;
        List<TunableConnection> objConnections = new ArrayList<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Tunable.class)) {
                field.setAccessible(true);
                try {
                    String fullPath = path + field.getName();
                    LoggedValue loggedValue = createLoggedValue(field, obj, fullPath);

                    if (loggedValue != null) {
                        objConnections.add(new TunableConnection(field, obj, loggedValue));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        registry.put(obj, objConnections);
    }

    /**
     * Maps Java fields to the correct AdvantageKit LoggedNetwork type based on
     * their reflection type.
     * * @param field The field to evaluate.
     * 
     * @param obj  The object instance owning the field.
     * @param path The full NetworkTables path for this specific field.
     * @return A {@link LoggedValue} wrapper for the appropriate AdvantageKit type,
     *         or null if unsupported.
     * @throws IllegalAccessException If the field's initial value cannot be read.
     */
    private static LoggedValue createLoggedValue(Field field, Object obj, String path) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == double.class || type == Double.class) {
            var net = new LoggedNetworkNumber(path, field.getDouble(obj));
            return new LoggedValue() {
                public Object get() {
                    return net.get();
                }

                public void setField(Field f, Object p) throws IllegalAccessException {
                    f.setDouble(p, net.get());
                }
            };
        }
        if (type == boolean.class || type == Boolean.class) {
            var net = new LoggedNetworkBoolean(path, field.getBoolean(obj));
            return new LoggedValue() {
                public Object get() {
                    return net.get();
                }

                public void setField(Field f, Object p) throws IllegalAccessException {
                    f.setBoolean(p, net.get());
                }
            };
        }
        if (type == int.class || type == Integer.class) {
            var net = new LoggedNetworkNumber(path, field.getInt(obj));
            return new LoggedValue() {
                public Object get() {
                    return (int) net.get();
                }

                public void setField(Field f, Object p) throws IllegalAccessException {
                    f.setInt(p, (int) net.get());
                }
            };
        }
        if (type == String.class) {
            var net = new LoggedNetworkString(path, (String) field.get(obj));
            return new LoggedValue() {
                public Object get() {
                    return net.get();
                }

                public void setField(Field f, Object p) throws IllegalAccessException {
                    f.set(p, net.get());
                }
            };
        }
        return null; // Type not supported
    }

    /**
     * Iterates through all registered fields for a given object and syncs them.
     * Intended to be called periodically (e.g., inside a subsystem's periodic
     * loop).
     * * @param obj The object instance to check updates for.
     * 
     * @return true if at least one tunable field was updated during this cycle,
     *         false otherwise.
     */
    public static boolean checkChanged(Object obj) {
        if (!tuningModeEnabled)
            return false;
        boolean anyChanged = false;
        List<TunableConnection> connections = registry.get(obj);
        if (connections != null) {
            for (TunableConnection conn : connections) {
                if (conn.sync())
                    anyChanged = true;
            }
        }
        return anyChanged;
    }
}