package team6230.koiupstream.sysid;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public class SysIdRoutine {

    public enum Direction { FORWARD, BACKWARD }

    private final SysIdMechanism mechanism;
    private final SysIdCollector collector;
    private final double minPosition;
    private final double maxPosition;
    private final double quasistaticRampRate;
    private final double dynamicStepVolts;
    private final double safetyMaxVolts;

    private SysIdRoutine(Builder builder) {
        this.mechanism            = builder.mechanism;
        this.collector            = builder.collector;
        this.minPosition          = builder.minPosition;
        this.maxPosition          = builder.maxPosition;
        this.quasistaticRampRate  = builder.rampRate;
        this.dynamicStepVolts     = builder.dynamicStep;
        this.safetyMaxVolts       = builder.maxVolts;
    }

    public Command quasistatic(Direction dir) {
        double sign = dir == Direction.FORWARD ? 1.0 : -1.0;
        Timer timer = new Timer();

        return Commands.sequence(
            Commands.runOnce(() -> {
                collector.startNewTest(SysIdTestType.QUASISTATIC, dir);
                timer.restart();
            }),
            Commands.run(() -> {
                double volts = sign * Math.min(
                    quasistaticRampRate * timer.get(),
                    safetyMaxVolts
                );
                mechanism.setVoltage(volts);
                collector.record(volts, mechanism.getVelocity(), mechanism.getPosition());
            })
            .until(() -> isOutsideBounds(mechanism.getPosition())),
            Commands.runOnce(() -> {
                mechanism.stopMotor();
                collector.commitTest();
            })
        );
    }

    public Command dynamic(Direction dir) {
        double volts = dir == Direction.FORWARD ? dynamicStepVolts : -dynamicStepVolts;

        return Commands.sequence(
            Commands.runOnce(() ->
                collector.startNewTest(SysIdTestType.DYNAMIC, dir)),
            Commands.run(() -> {
                mechanism.setVoltage(volts);
                collector.record(volts, mechanism.getVelocity(), mechanism.getPosition());
            })
            .until(() -> isOutsideBounds(mechanism.getPosition())),
            Commands.runOnce(() -> {
                mechanism.stopMotor();
                collector.commitTest();
            })
        );
    }

    private boolean isOutsideBounds(double position) {
        return position <= minPosition || position >= maxPosition;
    }

    public static class Builder {
        private final SysIdMechanism mechanism;
        private final SysIdCollector collector;
        private double minPosition   = Double.NEGATIVE_INFINITY;
        private double maxPosition   = Double.POSITIVE_INFINITY;
        private double rampRate      = 0.25;
        private double dynamicStep   = 4.0;
        private double maxVolts      = 7.0;

        public Builder(SysIdMechanism mechanism, SysIdCollector collector) {
            this.mechanism = mechanism;
            this.collector = collector;
        }

        public Builder withTravelWindow(double min, double max) {
            this.minPosition = min;
            this.maxPosition = max;
            return this;
        }

        public Builder withRampRate(double rampRate) {
            this.rampRate = rampRate;
            return this;
        }

        public Builder withDynamicStep(double stepVolts) {
            this.dynamicStep = stepVolts;
            return this;
        }

        public Builder withMaxVolts(double maxVolts) {
            this.maxVolts = maxVolts;
            return this;
        }

        public SysIdRoutine build() {
            return new SysIdRoutine(this);
        }
    }
}