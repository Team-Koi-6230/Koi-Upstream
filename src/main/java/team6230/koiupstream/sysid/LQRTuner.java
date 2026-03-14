package team6230.koiupstream.sysid;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.LinearQuadraticRegulator;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.system.plant.LinearSystemId;

public class LQRTuner {

    /**
     * Compute PID gains for a velocity mechanism (flywheel, etc.)
     *
     * @param kV             from SysId
     * @param kA             from SysId
     * @param toleranceVel   acceptable velocity error (native units/s)
     * @param toleranceVolts acceptable control effort (volts)
     * @param dtSeconds      loop period, usually 0.02
     * @return LQRResult with kP (kI and kD are 0 for velocity)
     */
    public static LQRResult forVelocity(
            double kV, double kA,
            double toleranceVel,
            double toleranceVolts,
            double dtSeconds) {

        LinearSystem<N1, N1, N1> plant = LinearSystemId.identifyVelocitySystem(kV, kA);

        LinearQuadraticRegulator<N1, N1, N1> lqr = new LinearQuadraticRegulator<>(
                plant,
                VecBuilder.fill(toleranceVel), // Q: state cost
                VecBuilder.fill(toleranceVolts), // R: effort cost
                dtSeconds);

        double kP = lqr.getK().get(0, 0);
        return new LQRResult(kP, 0.0, 0.0);
    }

    /**
     * Compute PID gains for a position mechanism (arm, elevator, etc.)
     *
     * @param kV             from SysId
     * @param kA             from SysId
     * @param tolerancePos   acceptable position error (native units)
     * @param toleranceVel   acceptable velocity error (native units/s)
     * @param toleranceVolts acceptable control effort (volts)
     * @param dtSeconds      loop period, usually 0.02
     * @return LQRResult with kP and kD (kI is 0, rarely needed with good FF)
     */
    public static LQRResult forPosition(
            double kV, double kA,
            double tolerancePos,
            double toleranceVel,
            double toleranceVolts,
            double dtSeconds) {

        LinearSystem<N2, N1, N2> plant = LinearSystemId.identifyPositionSystem(kV, kA);

        LinearQuadraticRegulator<N2, N1, N2> lqr = new LinearQuadraticRegulator<>(
                plant,
                VecBuilder.fill(tolerancePos, toleranceVel),
                VecBuilder.fill(toleranceVolts),
                dtSeconds);

        double kP = lqr.getK().get(0, 0);
        double kD = lqr.getK().get(0, 1);
        return new LQRResult(kP, 0.0, kD);
    }
}