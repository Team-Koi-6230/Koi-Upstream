package team6230.koiupstream.mechanisms.roller;

import team6230.koiupstream.mechanisms.roller.io.RollerIO;
import team6230.koiupstream.mechanisms.roller.io.RollerIOSim;
import team6230.koiupstream.mechanisms.roller.io.RollerIOSparkFlex;
import team6230.koiupstream.mechanisms.roller.io.RollerIOSparkMax;
import team6230.koiupstream.mechanisms.roller.io.RollerInputsAutoLogged;
import team6230.koiupstream.subsystems.UpstreamSubsystem;
import team6230.koiupstream.util.motorutil.MotorTypeHelper.motorType;

public class Roller<S extends Enum<S>> extends UpstreamSubsystem<S, RollerIO, RollerInputsAutoLogged> {
    private double rpm = 0;

    public Roller(RollerParameters params) {
        super(params.kName, getIO(params), new RollerInputsAutoLogged());
    }

    @Override
    public void update() {
        io.setTargetRPM(rpm);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private static RollerIO getIO(RollerParameters params) {
        if (params.kMotorType == motorType.SIM) return new RollerIOSim(params);
        if (params.kMotorType == motorType.FLEX) return new RollerIOSparkFlex(params);
        return new RollerIOSparkMax(params);
    }
}