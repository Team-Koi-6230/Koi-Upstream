package team6230.koiupstream.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import team6230.koiupstream.io.UpstreamIO;
import team6230.koiupstream.io.UpstreamIOAutoLogged;

import org.littletonrobotics.junction.Logger;

public abstract class UpstreamSubsystem extends SubsystemBase {
    private final UpstreamIO io;
    protected final UpstreamIOAutoLogged inputs;

    public UpstreamSubsystem(UpstreamIO io, UpstreamIOAutoLogged inputs) {

        this.io = io;
        this.inputs = inputs;
    }

    @Override
    public void periodic() {
        updateInputs();
        Logger.processInputs("Upstream/" + getName(), inputs);
    }

    public abstract void updateInputs();

    public abstract <E extends Enum<E>> void HandleSuperstate(E state);
    
    public abstract boolean isReady();
}
