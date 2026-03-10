package team6230.koiupstream.mechanisms.roller;

import edu.wpi.first.math.system.plant.DCMotor;
import team6230.koiupstream.util.motorutil.FollowerMotor;
import team6230.koiupstream.util.motorutil.MotorTypeHelper;
import team6230.koiupstream.util.motorutil.MotorTypeHelper.motorType;

public class RollerParameters {
    public String kName;

    public int kMotorId = 0;

    public FollowerMotor[] kFollowerMotors = {};

    public motorType kMotorType = MotorTypeHelper.getType(motorType.MAX);

    public DCMotor kSimMotor = DCMotor.getNEO(1 + kFollowerMotors.length);
    public double kJ = 0.05;

    public double kS = 0;
    public double kV = 0;
    public double kA = 0;
    public double kP = 0;
    public double kI = 0;
    public double kD = 0; 

    public int kSmartCurrentLimit = 40;

    public int kGearRatio = 1;
}
