package team6230.koiupstream.sysid;

public record SysIdResult(double kS, double kV, double kA) {

    public void publishToNT(String mechanismName) {
        var table = NetworkTableInstance.getDefault()
            .getTable("KoiUpstream/SysId/" + mechanismName);

        table.getEntry("kS").setDouble(kS);
        table.getEntry("kV").setDouble(kV);
        table.getEntry("kA").setDouble(kA);
        table.getEntry("suggested_kP").setDouble(kV > 0 ? 1.0 / kV : 0.0);
    }

    @Override
    public String toString() {
        return String.format("SysIdResult { kS=%.4f, kV=%.4f, kA=%.4f }", kS, kV, kA);
    }
}