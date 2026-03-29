package team6230.koiupstream.utils;

import java.util.function.DoubleSupplier;

public record SwerveInputStream(DoubleSupplier x, DoubleSupplier y, DoubleSupplier omega) {
}