package team6230.koiupstream.mechanisms.roller;

import edu.wpi.first.wpilibj.RobotBase;
import team6230.koiupstream.mechanisms.roller.io.RollerInputsAutoLogged;
import team6230.koiupstream.subsystems.UpstreamSubsystem;

public class Roller<S extends Enum<S>> extends UpstreamSubsystem<S, RollerInputsAutoLogged> {

    public Roller(RollerParameters params) {

    }
    
    @Override
    public void update() {

    }

    @Override
    public boolean isReady() {
        return true; 
    }
}