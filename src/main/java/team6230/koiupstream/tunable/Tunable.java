package team6230.koiupstream.tunable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark fields that should be dynamically tunable via
 * NetworkTables or AdvantageKit.
 * Fields marked with this annotation are automatically detected, published, and
 * synchronized
 * during runtime by the {@link TunableManager}.
 * <p>
 * Ensure the field type is supported by the manager (e.g., double, int,
 * boolean, String).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tunable {
    /**
     * An optional description of what the tunable value controls or represents.
     * * @return The description string, defaulting to an empty string.
     */
    String description() default "";
}