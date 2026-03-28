package team6230.koiupstream.subsystems;

import org.littletonrobotics.junction.inputs.LoggableInputs;

import team6230.koiupstream.io.UpstreamIO;

public record ExtraIO(@SuppressWarnings("rawtypes") UpstreamIO io, LoggableInputs inputs, String name) {
    
}
