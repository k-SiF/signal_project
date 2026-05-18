package com.cardio_generator.outputs;

/**
 * Defines the contract for delivering generated patient data to a destination.
 *
 * <p>This interface is the abstraction at the heart of the Strategy design
 * pattern as applied to the simulator's output side. Concrete implementations
 * decide <em>where</em> data goes (console, file, TCP socket, WebSocket) but
 * all expose the same {@link #output} method, so the rest of the system stays
 * agnostic to transport. New output destinations can be added without changing
 * any generator code.
 *
 * @author 6439058
 */
public interface OutputStrategy {

    /**
     * Sends a single labeled measurement for a patient to the underlying sink.
     *
     * @param patientId the integer ID of the patient the data belongs to;
     *                  assigned by the simulator, expected to be positive
     * @param timestamp epoch-millisecond timestamp when the measurement was
     *                  generated; should be non-decreasing within a single
     *                  patient's stream
     * @param label     the type of measurement (e.g. {@code "ECG"},
     *                  {@code "BloodPressure"}, {@code "Alert"}); never null
     * @param data      the measurement value formatted as a string
     *                  (e.g. {@code "120/80"}, {@code "97.5"},
     *                  {@code "triggered"}); never null
     */
    void output(int patientId, long timestamp, String label, String data);
}
