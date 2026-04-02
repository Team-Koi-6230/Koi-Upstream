package team6230.koiupstream.subsystems;

import java.util.function.BooleanSupplier;

/**
 * A record that pairs a condition with an action.
 * Useful for scheduling actions to occur once a specific condition is met
 * within the subsystem's periodic loop.
 *
 * @param condition A {@link BooleanSupplier} that must evaluate to true for the
 *                  action to run.
 * @param Action    A {@link Runnable} to execute when the condition becomes
 *                  true.
 */
public record ConditionalAction(BooleanSupplier condition, Runnable Action) {
}