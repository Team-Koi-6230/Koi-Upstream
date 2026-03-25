package team6230.koiupstream.io;

import org.littletonrobotics.junction.AutoLog;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import team6230.koiupstream.tunable.TunableManager;

public abstract class UpstreamIO<I extends LoggableInputs> {
    public UpstreamIO(String name) {
        TunableManager.register(this, "/Tuning/" + name + "/");
    }

    @AutoLog
    public static class UpstreamIOInputs {

    }

    public abstract void updateInputs(I inputs);
}