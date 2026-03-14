package team6230.koiupstream.tunable;

import java.lang.reflect.Field;
import java.util.*;
import org.littletonrobotics.junction.networktables.*;

/**
 * Universal manager for {@code @Tunable} annotations.
 * Automatically links class fields to AdvantageKit/NetworkTables and handles live syncing.
 */
public class TunableManager {
    public static boolean tuningModeEnabled = true;

    /**
     * Internal interface to unify different AdvantageKit LoggedNetwork types.
     */
    private interface LoggedValue {
        Object get();
        void setField(Field field, Object parent) throws IllegalAccessException;
    }

    private record TunableConnection(Field field, Object parent, LoggedValue networkValue) {
        private static final Map<Field, Object> lastValues = new HashMap<>();

        /**
         * Syncs the dashboard value to the Java field.
         * @return true if the value has changed since the last check.
         */
        boolean sync() {
            try {
                Object currentValue = networkValue.get();
                Object lastValue = lastValues.get(field);

                if (!Objects.equals(currentValue, lastValue)) {
                    networkValue.setField(field, parent);
                    lastValues.put(field, currentValue);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private static final Map<Object, List<TunableConnection>> registry = new HashMap<>();

    /**
     * Scans an object for {@code @Tunable} fields and registers them to NetworkTables.
     * Supports double, int, boolean, and String.
     * * @param obj The class instance to scan (usually 'this').
     * @param path The NetworkTables root path (e.g., "/Tuning/Intake/").
     */
    public static void register(Object obj, String path) {
        if (!tuningModeEnabled) return;
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
     * Maps Java fields to the correct AdvantageKit LoggedNetwork type.
     */
    private static LoggedValue createLoggedValue(Field field, Object obj, String path) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == double.class || type == Double.class) {
            var net = new LoggedNetworkNumber(path, field.getDouble(obj));
            return new LoggedValue() {
                public Object get() { return net.get(); }
                public void setField(Field f, Object p) throws IllegalAccessException { f.setDouble(p, net.get()); }
            };
        } 
        if (type == boolean.class || type == Boolean.class) {
            var net = new LoggedNetworkBoolean(path, field.getBoolean(obj));
            return new LoggedValue() {
                public Object get() { return net.get(); }
                public void setField(Field f, Object p) throws IllegalAccessException { f.setBoolean(p, net.get()); }
            };
        }
        if (type == int.class || type == Integer.class) {
            var net = new LoggedNetworkNumber(path, field.getInt(obj));
            return new LoggedValue() {
                public Object get() { return (int) net.get(); }
                public void setField(Field f, Object p) throws IllegalAccessException { f.setInt(p, (int) net.get()); }
            };
        }
        if (type == String.class) {
            var net = new LoggedNetworkString(path, (String) field.get(obj));
            return new LoggedValue() {
                public Object get() { return net.get(); }
                public void setField(Field f, Object p) throws IllegalAccessException { f.set(p, net.get()); }
            };
        }
        return null; // Type not supported
    }

    /**
     * Checks if any tunables in the object have changed and updates the Java fields.
     * * @param obj The object to check.
     * @return true if at least one field was updated this loop.
     */
    public static boolean checkChanged(Object obj) {
        if (!tuningModeEnabled) return false;
        boolean anyChanged = false;
        List<TunableConnection> connections = registry.get(obj);
        if (connections != null) {
            for (TunableConnection conn : connections) {
                if (conn.sync()) anyChanged = true;
            }
        }
        return anyChanged;
    }
}