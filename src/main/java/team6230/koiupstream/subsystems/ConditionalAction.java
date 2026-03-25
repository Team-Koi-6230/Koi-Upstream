package team6230.koiupstream.subsystems;

import java.util.function.BooleanSupplier;

public record ConditionalAction(BooleanSupplier condition, Runnable Action) {
}
