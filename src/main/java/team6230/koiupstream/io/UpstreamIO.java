package team6230.koiupstream.io;

import org.littletonrobotics.junction.AutoLog;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public interface UpstreamIO<I extends LoggableInputs> {
    @AutoLog
    public static class UpstreamIOInputs {
        public double appliedVolts = 0.0;
        public double[] currentAmps = new double[] {};
        public double[] tempCelsius = new double[] {};
    }

    public abstract void updateInputs(I inputs);
}