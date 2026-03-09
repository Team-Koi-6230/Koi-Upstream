package team6230.koiupstream.io;

import org.littletonrobotics.junction.AutoLog;

public interface UpstreamIO {
    @AutoLog
    public static class UpstreamIOInputs {
        public double appliedVolts = 0.0;
        public double[] currentAmps = new double[] {};
        public double[] tempCelsius = new double[] {};
    }

    public default void updateInputs(UpstreamIOInputs inputs) {}
}