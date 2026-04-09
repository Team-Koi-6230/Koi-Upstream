package team6230.koiupstream.utils;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DutyCycleEncoder;

public class AbsoluteEncoderDIO {
    private final DutyCycleEncoder _encoder;

    private Rotation2d _offset = new Rotation2d();
    private Rotation2d _minValue = new Rotation2d();

    public AbsoluteEncoderDIO(int port) {
        _encoder = new DutyCycleEncoder(port);
    }

    public AbsoluteEncoderDIO setEncoderOffset(Rotation2d offset) {
        _offset = new Rotation2d(offset.getRadians());
        return this;
    }

    public AbsoluteEncoderDIO setMinimumValue(Rotation2d minValue) {
        _minValue = new Rotation2d(minValue.getRadians());
        return this;
    }

    public AbsoluteEncoderDIO setInverted(boolean inverted) {
        _encoder.setInverted(inverted);
        return this;
    }

    public Rotation2d getPosition() {
        double val = _encoder.get() - _offset.getRotations();

        double wrapped = MathUtil.inputModulus(
                val,
                _minValue.getRotations(),
                _minValue.getRotations() + 1.0);

        return Rotation2d.fromRotations(wrapped);
    }

    public boolean isConnected() {
        return _encoder.isConnected();
    }
}
