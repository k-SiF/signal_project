package com.cardio_generator.outputs;

/**
 * Prints generated patient data to {@code System.out}, one line per measurement.
 *
 * <p>The simplest of the available output strategies, used by default when no
 * {@code --output} flag is provided to the simulator. Mainly useful for
 * development, demos, and smoke-testing the data pipeline.
 *
 * @author 6439058
 */
public class ConsoleOutputStrategy implements OutputStrategy {

    /**
     * Prints one measurement to standard output in a human-readable format.
     *
     * @param patientId the patient identifier
     * @param timestamp epoch millis when the measurement was taken
     * @param label     the measurement type (e.g. {@code "ECG"})
     * @param data      the formatted measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        System.out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                patientId, timestamp, label, data);
    }
}
