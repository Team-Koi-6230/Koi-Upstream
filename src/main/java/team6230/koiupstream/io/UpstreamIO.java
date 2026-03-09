package team6230.koiupstream.io;

import org.littletonrobotics.junction.AutoLog;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import team6230.koiupstream.util.tunable.TunableManager;

public abstract class UpstreamIO<I extends LoggableInputs> {
    public UpstreamIO(String name) {
        TunableManager.register(this, "/Tuning/" + name + "/");
    }

    @AutoLog
    public static class UpstreamIOInputs {
        public double appliedVolts = 0.0;
        public double[] currentAmps = new double[] {};
        public double[] tempCelsius = new double[] {};
    }

    public abstract void updateInputs(I inputs);
}