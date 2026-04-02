package team6230.koiupstream.utils;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

/**
 * A custom Xbox controller wrapper designed specifically for smooth swerve
 * drive operations.
 * It automatically applies deadbands, cubic curve shaping for fine control, and
 * {@link SlewRateLimiter}s to prevent harsh acceleration or deceleration that
 * could
 * damage the robot or cause the wheels to slip.
 */
public class KoiController extends CommandXboxController {

    // Slew rate limiters have a "memory" of the last speed, so they must be objects
    // here
    private final SlewRateLimiter translationLimiter;
    private final SlewRateLimiter strafeLimiter;
    private final SlewRateLimiter rotationLimiter;

    private final double deadband;

    /**
     * Constructs a new KoiController.
     *
     * @param port            The driver station USB port the controller is plugged
     *                        into.
     * @param deadband        The joystick deadband (usually 0.05 to 0.1) to ignore
     *                        stick drift.
     * @param translationRate The maximum rate of change (units per second) for
     *                        translational movement.
     * @param rotationRate    The maximum rate of change (units per second) for
     *                        rotational movement.
     */
    public KoiController(int port, double deadband, double translationRate, double rotationRate) {
        super(port);
        this.deadband = deadband;

        // Initialize the limiters
        this.translationLimiter = new SlewRateLimiter(translationRate);
        this.strafeLimiter = new SlewRateLimiter(translationRate);
        this.rotationLimiter = new SlewRateLimiter(rotationRate);
    }

    /**
     * Shapes the raw joystick input with a deadband and a cubic curve.
     * * @param rawValue The raw input axis value from the controller.
     * 
     * @return The smoothed and shaped input value.
     */
    private double shapeInput(double rawValue) {
        // 1. Cut off the stick drift
        double cleaned = MathUtil.applyDeadband(rawValue, deadband);
        // 2. Cube it for fine control (x * x * x preserves the negative/positive sign!)
        return cleaned * cleaned * cleaned;
    }

    /**
     * Gets the smoothed forward/backward translation input for the swerve drive.
     * <p>
     * <b>Note:</b> WPILib expects Forward to be POSITIVE X. The Xbox left Y-axis
     * is conventionally negative when pushed up, so this method automatically
     * negates it.
     *
     * @return The rate-limited and shaped forward/backward input.
     */
    public double getSwerveDrive() {
        return translationLimiter.calculate(shapeInput(-getLeftY()));
    }

    /**
     * Gets the smoothed left/right translation input for the swerve drive.
     * <p>
     * <b>Note:</b> WPILib expects Left to be POSITIVE Y. The Xbox left X-axis
     * is conventionally positive when pushed right, so this method automatically
     * negates it.
     *
     * @return The rate-limited and shaped left/right (strafe) input.
     */
    public double getSwerveStrafe() {
        return strafeLimiter.calculate(shapeInput(-getLeftX()));
    }

    /**
     * Gets the smoothed rotation input for the swerve drive.
     * <p>
     * <b>Note:</b> WPILib expects Counter-Clockwise to be POSITIVE. The Xbox right
     * X-axis is conventionally positive when pushed right (clockwise), so this
     * method
     * automatically negates it.
     *
     * @return The rate-limited and shaped rotational input.
     */
    public double getSwerveTurn() {
        return rotationLimiter.calculate(shapeInput(-getRightX()));
    }
}