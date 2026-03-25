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
    private ArrayList<ConditionalAction> conditionalActions = new ArrayList<>();

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
        checkConditionalActions();
        update();
    }

    public abstract void update();

    public final void handleSuperstate(S state) {
        Runnable stateReaction = stateReactions.get(state);
        if (stateReaction != null) {
            stateReaction.run();
        }
    }

    protected final void addSuperstateBehaviour(S state, Runnable action) {
        stateReactions.put(state, action);
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
                c.Action();
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
}