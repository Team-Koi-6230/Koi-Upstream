package team6230.koiupstream.mechanisms.roller;

import team6230.koiupstream.mechanisms.roller.io.RollerIO;
import team6230.koiupstream.mechanisms.roller.io.RollerInputsAutoLogged;
import team6230.koiupstream.subsystems.UpstreamSubsystem;
import team6230.koiupstream.util.MotorTypeHelper.motorType;

public class Roller<S extends Enum<S>> extends UpstreamSubsystem<S, RollerIO, RollerInputsAutoLogged> {
    private double rpm = 0;

    public Roller(RollerParameters params) {
        super(params.kName, getIO(params.kMotorType), new RollerInputsAutoLogged());
    }

    @Override
    public void update() {
        io.setTargetRPM(rpm);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private static RollerIO getIO(motorType m) {
        return null;
    }
}