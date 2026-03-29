package team6230.koiupstream.subsystems;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import team6230.koiupstream.superstates.Superstate;
import team6230.koiupstream.utils.DriveInputConsumer;
import team6230.koiupstream.utils.SwerveInputStream;

public abstract class UpstreamDrivebase<S extends Enum<S>> extends SubsystemBase {

    private SwerveInputStream _inputStream;

    private Map<S, DriveInputConsumer> _driveModes = new HashMap<>();
    private DriveInputConsumer _defaultDrive;

    public UpstreamDrivebase(SwerveInputStream input) {
        _inputStream = input;
    }

    protected void registerDefaultDrive(DriveInputConsumer di) {
        this._defaultDrive = di;
    }

    protected void registerDriveMode(S state, DriveInputConsumer di) {
        _driveModes.put(state, di);
    }

    protected abstract void updateInputs();

    @Override
    public final void periodic() {
        if (_defaultDrive == null) {
            throw new IllegalStateException("UpstreamDrivebase requires a Default Drive to be registered!");
        }

        updateInputs();
        var currentWantedState = Superstate.getInstance().getWantedSuperstate();

        DriveInputConsumer driveMethod = _driveModes.getOrDefault(currentWantedState, _defaultDrive);

        ChassisSpeeds wantedChassisSpeed = driveMethod.accept(_inputStream.x(), _inputStream.y(), _inputStream.omega());

        if (wantedChassisSpeed == null) {
            wantedChassisSpeed = new ChassisSpeeds();
        }

        runVelocity(wantedChassisSpeed);
    }

    public abstract void runVelocity(ChassisSpeeds speeds);
}
