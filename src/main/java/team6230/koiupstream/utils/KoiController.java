package team6230.koiupstream.utils;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

public class KoiController extends CommandXboxController {

    // Slew rate limiters have a "memory" of the last speed, so they must be objects
    // here
    private final SlewRateLimiter translationLimiter;
    private final SlewRateLimiter strafeLimiter;
    private final SlewRateLimiter rotationLimiter;

    private final double deadband;

    /**
     * @param port            The driver station port
     * @param deadband        The joystick deadband (usually 0.05 to 0.1)
     * @param translationRate Max units per second the translation can change
     * @param rotationRate    Max units per second the rotation can change
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
     */
    private double shapeInput(double rawValue) {
        // 1. Cut off the stick drift
        double cleaned = MathUtil.applyDeadband(rawValue, deadband);
        // 2. Cube it for fine control (x * x * x preserves the negative/positive sign!)
        return cleaned * cleaned * cleaned;
    }

    /**
     * Gets the smoothed forward/backward translation.
     * WPILib expects Forward to be POSITIVE X. Xbox left Y is negative when pushed
     * up.
     */
    public double getSwerveDrive() {
        return translationLimiter.calculate(shapeInput(-getLeftY()));
    }

    /**
     * Gets the smoothed left/right translation.
     * WPILib expects Left to be POSITIVE Y. Xbox left X is positive when pushed
     * right.
     */
    public double getSwerveStrafe() {
        return strafeLimiter.calculate(shapeInput(-getLeftX()));
    }

    /**
     * Gets the smoothed rotation.
     * WPILib expects Counter-Clockwise to be POSITIVE. Xbox right X is positive
     * when pushed right.
     */
    public double getSwerveTurn() {
        return rotationLimiter.calculate(shapeInput(-getRightX()));
    }
}