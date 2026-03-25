package team6230.koiupstream.superstates;

import java.util.logging.Logger;

import edu.wpi.first.wpilibj.DriverStation;
import team6230.koiupstream.subsystems.UpstreamSubsystem;

/* YOU MUST ENSURE THIS IS THE FIRST SUBSYSTEM TO BE CREATED. PREFERABLE IN THE ROBOT INIT */
public class Superstate {
    private static Superstate _instance;
    private final Logger logger = Logger.getLogger(Superstate.class.getName());

    private SuperstateManager<?> _manager;

    public enum DefaultStates {
        DEFAULT
    }

    private Superstate() {
        // Initialize with default
        _manager = new SuperstateManager<DefaultStates>(DefaultStates.DEFAULT);
    }

    public static Superstate getInstance() {
        if (_instance == null)
            _instance = new Superstate();
        return _instance;
    }

    /**
     * Re-instantiates the manager with a new Enum set.
     * WARNING: Call this BEFORE adding subsystems!
     */
    public <E extends Enum<E>> void setSuperstateSet(E statesSet) {
        _manager = new SuperstateManager<E>(statesSet);
    }

    public void addSubsystem(UpstreamSubsystem subsystem) {
        if (_manager.isDefault()) {
            DriverStation.reportWarning("WARNING: Subsystem manager is using default states", false);
            logger.warning("WARNING: Subsystem manager is using default states");
        }

        _manager.subscribe(subsystem::handleSuperstate, subsystem::isReady);
    }

    public void updateLogic() {
        _manager.periodic();
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E getSuperstate() {
        return (E) _manager.getSuperstate();
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E getWantedSuperstate() {
        return (E) _manager.getWantedSuperstate();
    }

    public boolean isAtSuperstate() {
        return _manager.isAtSuperstate();
    }
}
