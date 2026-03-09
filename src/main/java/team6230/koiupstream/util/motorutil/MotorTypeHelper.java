package team6230.koiupstream.util.motorutil;

import edu.wpi.first.wpilibj.RobotBase;

public class MotorTypeHelper {
    public enum motorType {
        MAX,
        FLEX,
        SIM
    }

    public static motorType getType(motorType m) {
        if (RobotBase.isSimulation()) return motorType.SIM;
        return m;
    }
}