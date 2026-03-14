package team6230.koiupstream.sysid;

import java.util.Optional;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public class AutoTuner {

    public static class Config {
        public double rampRate      = 0.25;
        public double dynamicStep   = 4.0;
        public double maxVolts      = 7.0;

        public double minPosition   = Double.NEGATIVE_INFINITY;
        public double maxPosition   = Double.POSITIVE_INFINITY;

        public MechanismType type         = MechanismType.VELOCITY;
        public double tolerancePos        = 0.02;
        public double toleranceVel        = 0.5;
        public double toleranceVolts      = 8.0;
        public double dtSeconds           = 0.02;

        public String mechanismName       = "Mechanism";
    }

    private final SysIdMechanism mechanism;
    private final SysIdCollector collector;
    private final SysIdRoutine routine;
    private final Config config;

    private AutoTuneResult lastResult = null;

    public AutoTuner(SysIdMechanism mechanism, Config config) {
        this.mechanism = mechanism;
        this.config    = config;
        this.collector = new SysIdCollector();
        this.routine   = new SysIdRoutine.Builder(mechanism, collector)
            .withTravelWindow(config.minPosition, config.maxPosition)
            .withRampRate(config.rampRate)
            .withDynamicStep(config.dynamicStep)
            .withMaxVolts(config.maxVolts)
            .build();
    }

    public Command quasistaticForward() {
        return routine.quasistatic(SysIdRoutine.Direction.FORWARD)
            .withName("SysId: Quasistatic Forward");
    }

    public Command quasistaticBackward() {
        return routine.quasistatic(SysIdRoutine.Direction.BACKWARD)
            .withName("SysId: Quasistatic Backward");
    }

    public Command dynamicForward() {
        return routine.dynamic(SysIdRoutine.Direction.FORWARD)
            .withName("SysId: Dynamic Forward");
    }

    public Command dynamicBackward() {
        return routine.dynamic(SysIdRoutine.Direction.BACKWARD)
            .withName("SysId: Dynamic Backward");
    }

    public Command analyze() {
        return Commands.runOnce(() -> {
            var data = collector.getAllData();

            // Validate all 4 runs exist before analyzing
            if (!data.containsKey("QUASISTATIC_FORWARD")  ||
                !data.containsKey("QUASISTATIC_BACKWARD") ||
                !data.containsKey("DYNAMIC_FORWARD")      ||
                !data.containsKey("DYNAMIC_BACKWARD")) {
                DriverStation.reportError(
                    "[AutoTuner] Missing test data — run all 4 routines first.", false);
                return;
            }

            SysIdResult ff = SysIdAnalyzer.analyze(
                data.get("QUASISTATIC_FORWARD"),
                data.get("QUASISTATIC_BACKWARD"),
                data.get("DYNAMIC_FORWARD"),
                data.get("DYNAMIC_BACKWARD")
            );

            LQRResult pid = config.type == MechanismType.VELOCITY
                ? LQRTuner.forVelocity(
                    ff.kV(), ff.kA(),
                    config.toleranceVel,
                    config.toleranceVolts,
                    config.dtSeconds)
                : LQRTuner.forPosition(
                    ff.kV(), ff.kA(),
                    config.tolerancePos,
                    config.toleranceVel,
                    config.toleranceVolts,
                    config.dtSeconds);

            lastResult = AutoTuneResult.from(ff, pid);
            lastResult.publishToNT(config.mechanismName);
            System.out.println("[AutoTuner] " + config.mechanismName);
            System.out.println(lastResult);
        }).withName("SysId: Analyze");
    }

    public Optional<AutoTuneResult> getLastResult() {
        return Optional.ofNullable(lastResult);
    }
}