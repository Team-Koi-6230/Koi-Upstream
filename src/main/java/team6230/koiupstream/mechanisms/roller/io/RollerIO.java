package team6230.koiupstream.mechanisms.roller.io;

import org.littletonrobotics.junction.AutoLog;

import team6230.koiupstream.io.UpstreamIO;
import team6230.koiupstream.mechanisms.roller.RollerParameters;

public abstract class RollerIO implements UpstreamIO<RollerInputsAutoLogged> {
    protected double currentRPMTarget = 0;

    public RollerIO(RollerParameters params) {
    }

    @AutoLog
    public static class RollerInputs {
        public double velocityRpm = 0.0;
        public double targetRpm = 0.0;
        public double appliedVolts = 0.0;
        public double[] currentAmps = new double[] {};
        public double[] tempCelsius = new double[] {};
        public double kP = 0.0;
        public double kI = 0.0;
        public double kD = 0.0;
        public double kS = 0.0;
        public double kV = 0.0;
    }

    public abstract void setTargetRPM(double rpm);

    public abstract void updateInputs(RollerInputsAutoLogged inputs);
}
