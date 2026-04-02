package team6230.koiupstream.superstates;

import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import team6230.koiupstream.subsystems.UpstreamSubsystem;

/**
 * A singleton subsystem responsible for managing and coordinating the overall
 * "Superstate"
 * of the robot. This class orchestrates state transitions across multiple
 * registered
 * subsystems, ensuring they act in unison based on a centralized state enum.
 * <p>
 * <b>CRITICAL:</b> YOU MUST ENSURE THIS IS THE FIRST SUBSYSTEM TO BE CREATED.
 * PREFERABLY IN THE ROBOT INIT.
 */
public class Superstate extends SubsystemBase {
    private static Superstate _instance;
    private final Logger logger = Logger.getLogger(Superstate.class.getName());

    private SuperstateManager<?> _manager;

    /**
     * The default fallback state enumeration used if a custom state set
     * is not provided via {@link #setSuperstateSet(Enum)}.
     */
    public enum DefaultStates {
        DEFAULT
    }

    /**
     * Private constructor to enforce the Singleton pattern.
     * Initializes the manager with the {@link DefaultStates} enum.
     */
    private Superstate() {
        // Initialize with default
        _manager = new SuperstateManager<DefaultStates>(DefaultStates.DEFAULT);
    }

    /**
     * Retrieves the singleton instance of the Superstate subsystem.
     * * @return The single instance of {@link Superstate}.
     */
    public static Superstate getInstance() {
        if (_instance == null)
            _instance = new Superstate();
        return _instance;
    }

    /**
     * Re-instantiates the manager with a new custom Enum set defining the robot's
     * states.
     * <p>
     * <b>WARNING:</b> Call this BEFORE adding any subsystems to avoid state
     * mismatches!
     * * @param <E> The Enum type representing the robot's superstates.
     * 
     * @param defaultState The initial state the robot should start in.
     */
    public <E extends Enum<E>> void setSuperstateSet(E defaultState) {
        _manager = new SuperstateManager<E>(defaultState);
    }

    /**
     * Registers an {@link UpstreamSubsystem} to be managed by the Superstate.
     * The subsystem will be subscribed to state changes and readiness checks.
     * * @param subsystem The subsystem to add to the superstate manager.
     */
    @SuppressWarnings("unchecked")
    public void addSubsystem(@SuppressWarnings("rawtypes") UpstreamSubsystem subsystem) {
        if (_manager.isDefault()) {
            DriverStation.reportWarning("WARNING: Subsystem manager is using default states", false);
            logger.warning("WARNING: Subsystem manager is using default states");
        }

        _manager.subscribe(subsystem::handleSuperstate, subsystem::isReady);
    }

    /**
     * Updates the internal logic of the state manager.
     * This should typically be called periodically (e.g., in
     * {@code robotPeriodic}).
     */
    public void updateLogic() {
        _manager.periodic();
    }

    /**
     * Retrieves the current, active superstate of the robot.
     * * @param <E> The Enum type representing the states.
     * 
     * @return The current superstate.
     */
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E getSuperstate() {
        return (E) _manager.getSuperstate();
    }

    /**
     * Retrieves the target superstate that the robot is currently trying to reach.
     * * @param <E> The Enum type representing the states.
     * 
     * @return The wanted superstate.
     */
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E getWantedSuperstate() {
        return (E) _manager.getWantedSuperstate();
    }

    /**
     * Checks if all registered subsystems have successfully transitioned to the
     * wanted superstate.
     * * @return {@code true} if the current state matches the wanted state and all
     * subsystems are ready,
     * {@code false} otherwise.
     */
    public boolean isAtSuperstate() {
        return _manager.isAtSuperstate();
    }

    /**
     * Requests a transition to a new superstate.
     * * @param <E> The Enum type representing the states.
     * 
     * @param wantedSuperstate The target state to transition the robot into.
     */
    public <E extends Enum<E>> void setWantedSuperstate(E wantedSuperstate) {
        _manager.setWantedSuperstate(wantedSuperstate);
    }

    /**
     * Creates a Command that sets the wanted superstate when executed.
     * * @param <E> The Enum type representing the states.
     * 
     * @param wantedSuperstate The target state to transition to.
     * @return A {@link Command} that updates the wanted superstate.
     */
    public <E extends Enum<E>> Command setWantedSuperstateCommand(E wantedSuperstate) {
        return run(() -> setWantedSuperstate(wantedSuperstate));
    }

    /**
     * Sets the default command for this subsystem to continuously request a
     * specific superstate.
     * * @param <E> The Enum type representing the states.
     * 
     * @param wantedSuperstate The default target state to fall back to.
     */
    public <E extends Enum<E>> void setDefaultWantedState(E wantedSuperstate) {
        this.setDefaultCommand(setWantedSuperstateCommand(wantedSuperstate));
    }

    /**
     * Safely checks if the current superstate matches a given target state.
     * * @param <E> The Enum type representing the states.
     * 
     * @param target The state to compare against.
     * @return {@code true} if the current state is equal to the target state,
     *         {@code false} otherwise.
     */
    public <E extends Enum<E>> boolean isCurrent(Enum<?> target) {
        var currentState = getSuperstate();
        if (target == null || currentState == null)
            return false;

        return currentState.getDeclaringClass().isInstance(target)
                && currentState.equals(target);
    }

    /**
     * Safely checks if the wanted (target) superstate matches a given state.
     * * @param <E> The Enum type representing the states.
     * 
     * @param target The state to compare against.
     * @return {@code true} if the wanted state is equal to the target state,
     *         {@code false} otherwise.
     */
    public <E extends Enum<E>> boolean isCurrentWanted(Enum<?> target) {
        var currentWantedState = getWantedSuperstate();
        if (target == null || currentWantedState == null)
            return false;

        return currentWantedState.getDeclaringClass().isInstance(target)
                && currentWantedState.equals(target);
    }

    /**
     * Provides a {@link BooleanSupplier} that evaluates to true when the current
     * superstate matches the specified target. Useful for command triggers or
     * WaitUntilCommands.
     * * @param target The state to wait for.
     * 
     * @return A BooleanSupplier evaluating the current state against the target.
     */
    public BooleanSupplier waitUntilCurrentIs(Enum<?> target) {
        return () -> isCurrent(target);
    }

    /**
     * Checks if the manager is currently in its configured default starting state.
     * * @return {@code true} if the robot is at the default state, {@code false}
     * otherwise.
     */
    public boolean isAtDefaultState() {
        return _manager.isAtDefaultState();
    }
}