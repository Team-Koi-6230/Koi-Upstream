package team6230.koiupstream.utils;

import java.util.function.DoubleSupplier;

/**
 * A record that bundles the three primary continuous inputs required for swerve
 * drive control.
 * This stream allows the drivebase to pull live data continuously without being
 * tightly
 * coupled to a specific controller instance.
 *
 * @param x     A {@link DoubleSupplier} providing the X-axis (forward/backward)
 *              translation input.
 * @param y     A {@link DoubleSupplier} providing the Y-axis (left/right)
 *              translation input.
 * @param omega A {@link DoubleSupplier} providing the rotational (turning)
 *              input.
 */
public record SwerveInputStream(DoubleSupplier x, DoubleSupplier y, DoubleSupplier omega) {
}