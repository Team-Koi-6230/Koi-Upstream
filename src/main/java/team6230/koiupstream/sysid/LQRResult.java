package team6230.koiupstream.sysid;

import edu.wpi.first.networktables.NetworkTableInstance;

public record LQRResult(double kP, double kI, double kD) {

    public void publishToNT(String mechanismName) {
        var table = NetworkTableInstance.getDefault()
            .getTable("KoiUpstream/SysId/" + mechanismName);

        table.getEntry("kP").setDouble(kP);
        table.getEntry("kI").setDouble(kI);
        table.getEntry("kD").setDouble(kD);
    }

    @Override
    public String toString() {
        return String.format("LQRResult { kP=%.4f, kI=%.4f, kD=%.4f }", kP, kI, kD);
    }
}