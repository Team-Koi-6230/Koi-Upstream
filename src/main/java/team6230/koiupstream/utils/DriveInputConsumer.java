package team6230.koiupstream.utils;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

/**
 * A functional interface used to define how swerve drive inputs should be
 * translated
 * into actual robot speeds depending on the active drive state.
 * <p>
 * This allows the swerve drive to react differently to driver inputs
 * (e.g., standard field-centric driving, robot-centric driving, auto-alignment
 * locking)
 * by passing these suppliers through different mathematical models before
 * returning
 * the final chassis speeds.
 */
@FunctionalInterface
public interface DriveInputConsumer {
    /**
     * Accepts the raw input streams and calculates the desired chassis speeds.
     *
     * @param t The X-axis translation input (forward/backward).
     * @param u The Y-axis translation input (left/right).
     * @param v The rotational input (turning).
     * @return The calculated {@link ChassisSpeeds} corresponding to the inputs.
     */
    ChassisSpeeds accept(DoubleSupplier t, DoubleSupplier u, DoubleSupplier v);
}