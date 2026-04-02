package team6230.koiupstream.subsystems;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import team6230.koiupstream.io.UpstreamIO;
import team6230.koiupstream.superstates.Superstate;
import team6230.koiupstream.tunable.TunableManager;

/**
 * The core abstract class for all subsystems in the Upstream architecture.
 * It integrates AdvantageKit logging, Superstate reactions, and conditional
 * action queues.
 *
 * @param <S>  The Enum type representing the robot's Superstates.
 * @param <io> The hardware abstraction interface extending {@link UpstreamIO}.
 * @param <I>  The AdvantageKit {@link LoggableInputs} type for this subsystem.
 */
public abstract class UpstreamSubsystem<S extends Enum<S>, io extends UpstreamIO<I>, I extends LoggableInputs>
        extends SubsystemBase {
    protected final io io;
    protected final I inputs;
    private Map<S, Runnable> stateReactions = new HashMap<>();
    private Runnable defaultReaction = null;
    private ArrayList<ConditionalAction> conditionalActions = new ArrayList<>();
    private Queue<ConditionalAction> actionsQueue = new ArrayDeque<>();
    private Queue<ConditionalAction> removeQueue = new ArrayDeque<>();
    private boolean requestCleanActions = false;
    private ArrayList<ExtraIO> extraIOs = new ArrayList<>();
    private boolean superstateMode = true;

    private String name;

    /**
     * Constructs a new UpstreamSubsystem, registering it with the Superstate
     * manager
     * and the TunableManager.
     *
     * @param name   The name of the subsystem used for logging and tuning paths.
     * @param inputs The instantiated LoggableInputs object for AdvantageKit.
     */
    public UpstreamSubsystem(String name, I inputs) {
        super();
        this.io = getIO();
        this.inputs = inputs;
        this.name = name;
        Superstate.getInstance().addSubsystem(this);
        TunableManager.register(this, "/Tuning/" + name + "/");
    }

    /**
     * The main periodic loop. Updates core IO, processes AdvantageKit logging,
     * handles extra IO blocks, evaluates conditional actions, and calls the
     * abstract update method.
     */
    @Override
    public final void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Upstream/" + getName(), inputs);
        handleExtraIOs();
        checkConditionalActions();
        update();

    }

    /**
     * Iterates through and processes inputs for any registered supplementary IO
     * layers.
     */
    @SuppressWarnings("unchecked")
    private void handleExtraIOs() {
        if (extraIOs.isEmpty())
            return;
        for (var io : extraIOs) {
            io.io().updateInputs(io.inputs());
            Logger.processInputs("Upstream/" + getName() + "/" + io.name(), io.inputs());
        }
    }

    /**
     * Registers an additional IO block to be updated and logged alongside the core
     * IO.
     *
     * @param extra The {@link ExtraIO} to add.
     */
    protected final void addAnExtraIO(ExtraIO extra) {
        extraIOs.add(extra);
    }

    /**
     * Abstract method called periodically to handle subsystem-specific logic.
     */
    public abstract void update();

    /**
     * Triggered by the Superstate manager to execute the reaction mapped to the
     * given state.
     *
     * @param state The current or wanted Superstate being evaluated.
     */
    public final void handleSuperstate(S state) {
        if (!superstateMode)
            return;
        Runnable stateReaction = stateReactions.get(state);
        if (stateReaction != null) {
            stateReaction.run();
        } else if (defaultReaction != null && !Superstate.getInstance().isAtDefaultState()) {
            defaultReaction.run();
        }
    }

    /**
     * Maps a specific action to occur when the subsystem is instructed to enter a
     * given Superstate.
     *
     * @param state  The target Superstate.
     * @param action The {@link Runnable} to execute.
     */
    protected final void addSuperstateBehaviour(S state, Runnable action) {
        stateReactions.put(state, action);
    }

    /**
     * Sets the default fallback action to run when no specific reaction is mapped
     * to the requested Superstate.
     *
     * @param action The fallback {@link Runnable}.
     */
    protected final void addDefaultSuperstateBehaviour(Runnable action) {
        defaultReaction = action;
    }

    /**
     * Queues a conditional action to be evaluated periodically.
     *
     * @param conditionalAction The {@link ConditionalAction} to evaluate.
     */
    protected final void registerConditionalAction(ConditionalAction conditionalAction) {
        actionsQueue.add(conditionalAction);
    }

    /**
     * Requests that all currently queued and active conditional actions be cleared.
     */
    protected final void clearConditionalActions() {
        requestCleanActions = true;
    }

    /**
     * Returns a cloned list of the currently active conditional actions.
     *
     * @return A shallow copy of the internal {@link ConditionalAction} list.
     */
    @SuppressWarnings("unchecked")
    protected final ArrayList<ConditionalAction> getConditionalActions() {
        return (ArrayList<ConditionalAction>) conditionalActions.clone();
    }

    /**
     * Queues a specific conditional action to be removed from the active list.
     *
     * @param removedAction The {@link ConditionalAction} to remove.
     */
    protected final void removeConditionalAction(ConditionalAction removedAction) {
        removeQueue.add(removedAction);

    }

    /**
     * Evaluates the queues for conditional actions. Safely removes requested
     * actions,
     * clears the list if requested, adds new actions, and evaluates conditions.
     * Actions that evaluate to true are executed and then removed from the list.
     */
    private void checkConditionalActions() {
        while (!removeQueue.isEmpty()) {
            try {
                conditionalActions.remove(removeQueue.remove());
            } catch (Exception e) {
                System.err.println("[ERROR]: Failed to remove action in subsystem " + getName());
            }
        }

        if (requestCleanActions) {
            conditionalActions.clear();
            requestCleanActions = false;
        }

        while (!actionsQueue.isEmpty()) {
            conditionalActions.add(actionsQueue.remove());
        }

        conditionalActions.removeIf(c -> {
            if (c.condition().getAsBoolean()) {
                c.Action().run();
                return true;
            }
            return false;
        });
    }

    /**
     * Abstract method to determine if this subsystem has successfully reached
     * its required state/position based on the current Superstate.
     *
     * @return {@code true} if ready, {@code false} otherwise.
     */
    public abstract boolean isReady();

    /**
     * Abstract method used to fetch the hardware IO implementation for this
     * subsystem.
     *
     * @return The instantiated {@link UpstreamIO} interface.
     */
    protected abstract io getIO();

    /**
     * Gets the name of the subsystem.
     *
     * @return The subsystem's name as a String.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this subsystem is currently responding to the Superstate manager.
     *
     * @return {@code true} if Superstate mode is enabled, {@code false} if
     *         manual/disabled.
     */
    protected final boolean isSuperstateMode() {
        return superstateMode;
    }

    /**
     * Toggles whether this subsystem should automatically react to Superstate
     * changes.
     * Useful for manual control or overrides.
     *
     * @param on {@code true} to enable Superstate reactions, {@code false} to
     *           disable.
     */
    protected final void setSuperstateMode(boolean on) {
        this.superstateMode = on;
    }
}