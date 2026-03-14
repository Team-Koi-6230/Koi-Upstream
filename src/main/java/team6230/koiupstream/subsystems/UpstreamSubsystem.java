package team6230.koiupstream.subsystems;

import java.util.HashMap;
import java.util.Map;

import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import team6230.koiupstream.io.UpstreamIO;
import team6230.koiupstream.superstates.Superstate;
import team6230.koiupstream.tunable.TunableManager;

public abstract class UpstreamSubsystem<S extends Enum<S>, io extends UpstreamIO<I>, I extends LoggableInputs> extends SubsystemBase {
    protected final io io;
    protected final I inputs;
    private Map<S, Runnable> stateReactions = new HashMap<>();

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
        update();
    }

    public abstract void update();

    public final void handleSuperstate(S state) {
        Runnable stateReaction = stateReactions.get(state);
        if (stateReaction != null) {
            stateReaction.run();
        }
    }

    public final void addSuperstateBehaviour(S state, Runnable action) {
        stateReactions.put(state, action);
    }

    // Indicate when the subsystem has achived the wanted state
    public abstract boolean isReady();

    protected abstract io getIO();

    public String getName() {
        return name;
    }
}