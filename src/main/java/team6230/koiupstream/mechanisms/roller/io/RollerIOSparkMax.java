package team6230.koiupstream.mechanisms.roller.io;

import java.util.ArrayList;
import java.util.List;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import org.littletonrobotics.junction.networktables.LoggedNetworkNumber;

import team6230.koiupstream.util.FollowerMotor;
import team6230.koiupstream.mechanisms.roller.RollerParameters;

public class RollerIOSparkMax extends RollerIO {
    private final SparkMax m_leader;
    private final List<SparkMax> m_followers = new ArrayList<>();
    private final RollerParameters m_params;

    private final LoggedNetworkNumber kP;
    private final LoggedNetworkNumber kI;
    private final LoggedNetworkNumber kD;
    private final LoggedNetworkNumber kS;
    private final LoggedNetworkNumber kV;
    private final LoggedNetworkNumber kA;

    private double lastKP, lastKI, lastKD, lastKS, lastKV, lastKA;

    public RollerIOSparkMax(RollerParameters params) {
        super(params);
        this.m_params = params;

        String handle = "/Tuning/" + params.kName + "/";

        kP = new LoggedNetworkNumber(handle + "kP", params.kP);
        kI = new LoggedNetworkNumber(handle + "kI", params.kI);
        kD = new LoggedNetworkNumber(handle + "kD", params.kD);
        kS = new LoggedNetworkNumber(handle + "kS", params.kS);
        kV = new LoggedNetworkNumber(handle + "kV", params.kV);
        kA = new LoggedNetworkNumber(handle + "kA", params.kA);

        m_leader = new SparkMax(params.kMotorId, MotorType.kBrushless);

        for (FollowerMotor f : params.kFollowerMotors) {
            SparkMax follower = new SparkMax(f.id(), MotorType.kBrushless);
            SparkMaxConfig fConfig = new SparkMaxConfig();
            fConfig.follow(params.kMotorId, f.isInverted());
            follower.configure(fConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
            m_followers.add(follower);
        }

        applyHardwareConfig();
    }

    private void applyHardwareConfig() {
        SparkMaxConfig config = new SparkMaxConfig();

        config.smartCurrentLimit(m_params.kSmartCurrentLimit).encoder
                .velocityConversionFactor(1.0 / m_params.kGearRatio);

        config.closedLoop.pid(kP.get(), kI.get(), kD.get()).feedForward.kS(kS.get()).kV(kV.get()).kA(kA.get());

        m_leader.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        lastKP = kP.get(); lastKI = kI.get(); lastKD = kD.get();
        lastKS = kS.get(); lastKV = kV.get(); lastKA = kA.get();
    }

    @Override
    public void updateInputs(RollerInputsAutoLogged inputs) {
        if (kP.get() != lastKP || kI.get() != lastKI || kD.get() != lastKD || 
            kS.get() != lastKS || kV.get() != lastKV || kA.get() != lastKA) {
            applyHardwareConfig();
        }

        inputs.velocityRpm = m_leader.getEncoder().getVelocity();
        inputs.appliedVolts = m_leader.getAppliedOutput() * m_leader.getBusVoltage();

        inputs.kP = kP.get();
        inputs.kI = kI.get();
        inputs.kD = kD.get();
        inputs.kS = kS.get();
        inputs.kV = kV.get();

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