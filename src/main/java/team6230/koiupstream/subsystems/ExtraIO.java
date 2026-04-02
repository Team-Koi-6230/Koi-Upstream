package team6230.koiupstream.subsystems;

import org.littletonrobotics.junction.inputs.LoggableInputs;

import team6230.koiupstream.io.UpstreamIO;

/**
 * A record representing supplementary IO layers for a subsystem.
 * This is primarily used for subsystems that require multiple hardware
 * interactions
 * (like secondary motors or sensors) that need to be logged independently via
 * AdvantageKit.
 *
 * @param io     The hardware abstraction layer (IO interface) to update.
 * @param inputs The AdvantageKit {@link LoggableInputs} object where data will
 *               be stored.
 * @param name   The logging path/name for this specific extra IO block.
 */
public record ExtraIO(@SuppressWarnings("rawtypes") UpstreamIO io, LoggableInputs inputs, String name) {

}