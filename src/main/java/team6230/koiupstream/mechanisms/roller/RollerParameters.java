package team6230.koiupstream.mechanisms.roller;

import team6230.koiupstream.util.FollowerMotor;
import team6230.koiupstream.util.MotorTypeHelper;
import team6230.koiupstream.util.MotorTypeHelper.motorType;

public class RollerParameters {
    public String kName;

    public int kMotorId = 0;

    public FollowerMotor[] kFollowerMotors = {};

    public motorType kMotorType = MotorTypeHelper.getType(motorType.MAX);

    public double kS = 0;
    public double kV = 0;
    public double kA = 0;
    public double kP = 0;
    public double kI = 0;
    public double kD = 0; 

    public int kSmartCurrentLimit = 40;

    public int kGearRatio = 1;

    public boolean kTuning = true;
}
