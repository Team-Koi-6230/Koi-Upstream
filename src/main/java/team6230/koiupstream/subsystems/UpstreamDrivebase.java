package team6230.koiupstream.subsystems;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import team6230.koiupstream.utils.DriveInputConsumer;
import team6230.koiupstream.utils.SwerveInputStream;

/**
 * An abstract drivebase subsystem integrated with the Superstate architecture.
 * This class automatically maps the robot's current Superstate to specific
 * driving
 * control schemas (e.g., field-centric, robot-centric, auto-aligning).
 *
 * @param <S> The Enum representing the overall robot states (Superstates).
 */
public abstract class UpstreamDrivebase<S extends Enum<S>> extends SubsystemBase {

    private SwerveInputStream _inputStream;

    private Map<S, DriveInputConsumer> _driveModes = new HashMap<>();
    private DriveInputConsumer _defaultDrive;
    private DriveInputConsumer _currentDrive;

    private boolean superstateMode = true;

    /**
     * Constructs a new UpstreamDrivebase.
     *
     * @param input The input stream that translates pilot controller inputs into
     *              drive signals.
     */
    public UpstreamDrivebase(SwerveInputStream input) {
        _inputStream = input;

    }

    /**
     * Triggered by the Superstate manager to execute the reaction mapped to the
     * given state.
     *
     * @param state The current or wanted Superstate being evaluated.
     */
    public final void handleSuperstate(S state) {
        if (!superstateMode)
            return;
        _currentDrive = _driveModes.getOrDefault(state, _defaultDrive);
    }

    public final void setSuperstateMode(boolean superstateMode) {
        this.superstateMode = superstateMode;
    }

    public abstract boolean isReady();

    /**
     * Registers the fallback driving mode. This mode is used if the current
     * Superstate does not have a explicitly registered drive mode.
     *
     * @param di The default {@link DriveInputConsumer}.
     */
    protected void registerDefaultDrive(DriveInputConsumer di) {
        this._defaultDrive = di;
        this._currentDrive = this._defaultDrive;
    }

    /**
     * Registers a specific driving mode to be used when the robot is in a given
     * Superstate.
     *
     * @param state The Superstate that triggers this drive mode.
     * @param di    The {@link DriveInputConsumer} defining how inputs are handled
     *              in this state.
     */
    protected void registerDriveMode(S state, DriveInputConsumer di) {
        _driveModes.put(state, di);
    }

    /**
     * Abstract method to be implemented by subclasses to update necessary sensor
     * inputs or odometry before calculating drive speeds.
     */
    protected abstract void updateInputs();

    /**
     * The main periodic loop for the drivebase.
     * It ensures a default drive is set, updates inputs, determines the desired
     * Superstate, calculates the required chassis speeds based on the mapped
     * drive consumer, and commands the robot to move.
     *
     * @throws IllegalStateException If no default drive mode has been registered.
     */
    @Override
    public final void periodic() {
        if (_defaultDrive == null) {
            throw new IllegalStateException("UpstreamDrivebase requires a Default Drive to be registered!");
        }

        updateInputs();

        ChassisSpeeds wantedChassisSpeed = _currentDrive.accept(_inputStream.x(), _inputStream.y(),
                _inputStream.omega());

        if (wantedChassisSpeed == null) {
            wantedChassisSpeed = new ChassisSpeeds();
        }

        runVelocity(wantedChassisSpeed);
    }

    /**
     * Abstract method to command the actual hardware to reach the calculated
     * speeds.
     *
     * @param speeds The desired {@link ChassisSpeeds} for the robot.
     */
    public abstract void runVelocity(ChassisSpeeds speeds);
}