package team6230.koiupstream.mechanisms.roller.io;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import team6230.koiupstream.mechanisms.roller.RollerParameters;
import team6230.koiupstream.util.tunable.Tunable;
import team6230.koiupstream.util.tunable.TunableManager;

public class RollerIOSim extends RollerIO {
    private double m_appliedVolts = 0;
    private final FlywheelSim m_sim;
    private PIDController pidController;
    private SimpleMotorFeedforward ffController;

    @Tunable
    public double kP;
    @Tunable
    public double kI;
    @Tunable
    public double kD;
    @Tunable
    public double kS;
    @Tunable
    public double kV;
    @Tunable
    public double kA;

    public RollerIOSim(RollerParameters params) {
        super(params);

        kP = params.kP;
        kI = params.kI;
        kD = params.kD;
        kS = params.kS;
        kA = params.kA;
        kV = params.kV;

        var flywheelPlant = LinearSystemId.createFlywheelSystem(
                params.kSimMotor,
                params.kJ,
                params.kGearRatio);

        m_sim = new FlywheelSim(flywheelPlant, params.kSimMotor);
        applyHardwareConfig();
    }

    public void applyHardwareConfig() {
        pidController = new PIDController(kP, kI, kD);
        ffController = new SimpleMotorFeedforward(kS, kV, kA);
    }

    @Override
    public void setTargetRPM(double rpm) {
        currentRPMTarget = rpm;
        setVoltage(getCalculatedVoltage());
    }

    private void setVoltage(double volts) {
        m_appliedVolts = Math.max(-12.0, Math.min(12.0, volts));
        m_sim.setInputVoltage(m_appliedVolts);
    }

    private double getCalculatedVoltage() {
        return pidController.calculate(m_sim.getAngularVelocityRPM(), currentRPMTarget) + ffController.calculate(currentRPMTarget);
    }

    @Override
    public void updateInputs(RollerInputsAutoLogged inputs) {
        m_sim.update(0.020);
        if (TunableManager.checkChanged(this)) {
            applyHardwareConfig();
        }

        inputs.velocityRpm = m_sim.getAngularVelocityRPM();
        inputs.appliedVolts = m_appliedVolts;

        inputs.currentAmps = new double[] { m_sim.getCurrentDrawAmps() };

        inputs.tempCelsius = new double[] { 25.0 + (Math.abs(m_appliedVolts) * 0.5) };
    }

}
