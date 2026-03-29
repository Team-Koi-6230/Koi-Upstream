package team6230.koiupstream.utils;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

/*
 * A functional interface for swerve drive states.
 * Allows the swerve to react differently to each state
 * Must take all 3 swerve inputs and return the desired chassis speeds
 */
@FunctionalInterface
public interface DriveInputConsumer {
    ChassisSpeeds accept(DoubleSupplier t, DoubleSupplier u, DoubleSupplier v);
}