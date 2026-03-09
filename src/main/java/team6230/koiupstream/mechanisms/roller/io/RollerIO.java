package team6230.koiupstream.mechanisms.roller.io;

import org.littletonrobotics.junction.AutoLog;

import team6230.koiupstream.io.UpstreamIO;
import team6230.koiupstream.mechanisms.roller.RollerParameters;

public abstract class RollerIO implements UpstreamIO<RollerInputsAutoLogged> {

    public RollerIO(RollerParameters params) {

    }

    @AutoLog
    public static class RollerInputs {
        public double rpm = 0.0;
        public double appliedVolts = 0.0;
        public double[] currentAmps = new double[] {};
    }

    public abstract void updateInputs(RollerInputsAutoLogged inputs);
    
}
