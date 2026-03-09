package team6230.koiupstream.mechanisms.roller.io;

import java.util.ArrayList;
import java.util.List;

import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;

import team6230.koiupstream.util.motorutil.FollowerMotor;
import team6230.koiupstream.util.tunable.Tunable;
import team6230.koiupstream.util.tunable.TunableManager;
import team6230.koiupstream.mechanisms.roller.RollerParameters;

public class RollerIOSparkFlex extends RollerIO {
    private final SparkFlex m_leader;
    private final List<SparkFlex> m_followers = new ArrayList<>();

    @Tunable public double kP;
    @Tunable public double kI;
    @Tunable public double kD;
    @Tunable public double kS;
    @Tunable public double kV;
    @Tunable public double kA;
    @Tunable public int kSmartCurrentLimit;
    @Tunable public double kGearRatio;
    

    public RollerIOSparkFlex(RollerParameters params) {
        super(params);

        kP = params.kP;
        kI = params.kI;
        kD = params.kD;
        kS = params.kS;
        kA = params.kA;
        kV = params.kV;
        kSmartCurrentLimit = params.kSmartCurrentLimit;
        kGearRatio = params.kGearRatio;

        m_leader = new SparkFlex(params.kMotorId, MotorType.kBrushless);

        for (FollowerMotor f : params.kFollowerMotors) {
            SparkFlex follower = new SparkFlex(f.id(), MotorType.kBrushless);
            SparkMaxConfig fConfig = new SparkMaxConfig();
            fConfig.follow(params.kMotorId, f.isInverted());
            follower.configure(fConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
            m_followers.add(follower);
        }

        applyHardwareConfig();
    }

    private void applyHardwareConfig() {
        SparkMaxConfig config = new SparkMaxConfig();

        config.closedLoop.pid(kP, kI, kD).feedForward.kS(kS).kV(kV).kA(kA);

        config.smartCurrentLimit(kSmartCurrentLimit).encoder
                .velocityConversionFactor(1.0 / kGearRatio);
    }

    @Override
    public void updateInputs(RollerInputsAutoLogged inputs) {
        if (TunableManager.checkChanged(this)) {
            applyHardwareConfig();
        }

        inputs.velocityRpm = m_leader.getEncoder().getVelocity();
        inputs.appliedVolts = m_leader.getAppliedOutput() * m_leader.getBusVoltage();

        double[] currents = new double[m_followers.size() + 1];
        double[] temps = new double[m_followers.size() + 1];

        currents[0] = m_leader.getOutputCurrent();
        temps[0] = m_leader.getMotorTemperature();

        for (int i = 0; i < m_followers.size(); i++) {
            currents[i + 1] = m_followers.get(i).getOutputCurrent();
            temps[i + 1] = m_followers.get(i).getMotorTemperature();
        }

        inputs.currentAmps = currents;
        inputs.tempCelsius = temps;
    }

    @Override
    public void setTargetRPM(double rpm) {
        if (currentRPMTarget == rpm)
            return;
        m_leader.getClosedLoopController().setSetpoint(currentRPMTarget, ControlType.kVelocity);
    }
}