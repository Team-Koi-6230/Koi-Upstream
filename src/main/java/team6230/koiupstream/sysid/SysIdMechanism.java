package team6230.koiupstream.sysid;

public interface SysIdMechanism {
    void setVoltage(double volts);
    double getVelocity();
    double getPosition();
    void stopMotor();
}