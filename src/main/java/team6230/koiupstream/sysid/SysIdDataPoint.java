package team6230.koiupstream.sysid;

public record SysIdDataPoint(
    double timestampSeconds,
    double appliedVolts,
    double velocityNative,
    double positionNative
) {}