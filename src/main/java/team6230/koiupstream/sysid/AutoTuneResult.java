package team6230.koiupstream.sysid;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;

public record AutoTuneResult(
    double kS,
    double kV,
    double kA,

    double kP,
    double kI,
    double kD
) {
    public static AutoTuneResult from(SysIdResult ff, LQRResult pid) {
        return new AutoTuneResult(
            ff.kS(), ff.kV(), ff.kA(),
            pid.kP(), pid.kI(), pid.kD()
        );
    }

    public void publishToNT(String mechanismName) {
        var table = NetworkTableInstance.getDefault()
            .getTable("KoiUpstream/AutoTuner/" + mechanismName);

        // Feedforward
        table.getEntry("kS").setDouble(kS);
        table.getEntry("kV").setDouble(kV);
        table.getEntry("kA").setDouble(kA);
        // PID
        table.getEntry("kP").setDouble(kP);
        table.getEntry("kI").setDouble(kI);
        table.getEntry("kD").setDouble(kD);
        // Timestamp so you know when it was last run
        table.getEntry("lastTunedTimestamp").setDouble(Timer.getFPGATimestamp());
    }

    @Override
    public String toString() {
        return String.format(
            "AutoTuneResult {\n  FF: kS=%.4f  kV=%.4f  kA=%.4f\n  PID: kP=%.4f  kI=%.4f  kD=%.4f\n}",
            kS, kV, kA, kP, kI, kD
        );
    }
}