package team6230.koiupstream.subsystems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import team6230.koiupstream.io.UpstreamIO;
import team6230.koiupstream.superstates.Superstate;
import team6230.koiupstream.tunable.TunableManager;

public abstract class UpstreamSubsystem<S extends Enum<S>, io extends UpstreamIO<I>, I extends LoggableInputs>
        extends SubsystemBase {
    protected final io io;
    protected final I inputs;
    private Map<S, Runnable> stateReactions = new HashMap<>();
    private Runnable defaultReaction = null;
    private ArrayList<ConditionalAction> conditionalActions = new ArrayList<>();
    private ArrayList<ExtraIO> extraIOs = new ArrayList<>();
    private boolean superstateMode = true;

    private String name;

    public UpstreamSubsystem(String name, I inputs) {
        super();
        this.io = getIO();
        this.inputs = inputs;
        this.name = name;
        Superstate.getInstance().addSubsystem(this);
        TunableManager.register(this, "/Tuning/" + name + "/");
    }

    @Override
    public final void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Upstream/" + getName(), inputs);
        handleExtraIOs();
        checkConditionalActions();
        update();
    }

    @SuppressWarnings("unchecked")
    private void handleExtraIOs() {
        if (extraIOs.isEmpty())
            return;
        for (var io : extraIOs) {
            io.io().updateInputs(io.inputs());
            Logger.processInputs("Upstream/" + getName() + "/" + io.name(), io.inputs());
        }
    }

    protected final void addAnExtraIO(ExtraIO extra) {
        extraIOs.add(extra);
    }

    public abstract void update();

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

    protected final void addSuperstateBehaviour(S state, Runnable action) {
        stateReactions.put(state, action);
    }

    protected final void addDefaultSuperstateBehaviour(Runnable action) {
        defaultReaction = action;
    }

    protected final void registerConditionalAction(ConditionalAction conditionalAction) {
        conditionalActions.add(conditionalAction);
    }

    protected final void clearConditionalActions() {
        conditionalActions.clear();
    }

    @SuppressWarnings("unchecked")
    protected final ArrayList<ConditionalAction> getConditionalActions() {
        return (ArrayList<ConditionalAction>) conditionalActions.clone();
    }

    protected final void removeConditionalAction(ConditionalAction removedAction) {
        try {
            conditionalActions.remove(removedAction);
        } catch (Exception e) {
            System.err.println("[ERROR]: Failed to remove action in subsystem " + getName());
        }
    }

    private void checkConditionalActions() {
        conditionalActions.removeIf(c -> {
            if (c.condition().getAsBoolean()) {
                c.Action().run();;

                return true;
            }
            return false;
        });
    }

    // Indicate when the subsystem has achived the wanted state
    public abstract boolean isReady();

    protected abstract io getIO();

    public String getName() {
        return name;
    }

    protected final boolean isSuperstateMode() {
        return superstateMode;
    }

    protected final void setSuperstateMode(boolean on) {
        this.superstateMode = on;
    }
}