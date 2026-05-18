package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates a simulated ECG waveform sample for each patient.
 *
 * <p>The waveform is synthesized from three sinusoidal components (P-wave,
 * QRS complex, and T-wave) at frequencies derived from a randomized heart
 * rate (60–80 bpm). A small amount of random noise is added each call.
 *
 * @author 6439058
 */
public class ECGDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private double[] lastEcgValues;
    private static final double PI = Math.PI;

    /**
     * Constructs the generator and initializes per-patient last-value storage.
     *
     * @param patientCount number of patients to support
     */
    public ECGDataGenerator(int patientCount) {
        lastEcgValues = new double[patientCount + 1];
        for (int i = 1; i <= patientCount; i++) {
            lastEcgValues[i] = 0;
        }
    }

    /**
     * Emits one ECG sample for the patient.
     *
     * @param patientId      patient identifier (1-based)
     * @param outputStrategy destination for the sample
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            double ecgValue = simulateEcgWaveform(patientId, lastEcgValues[patientId]);
            outputStrategy.output(patientId, System.currentTimeMillis(), "ECG", Double.toString(ecgValue));
            lastEcgValues[patientId] = ecgValue;
        } catch (Exception e) {
            System.err.println("An error occurred while generating ECG data for patient " + patientId);
            e.printStackTrace();
        }
    }

    /**
     * Computes one sample of a simplified ECG waveform.
     *
     * @param patientId    patient identifier (used implicitly via lastEcgValue)
     * @param lastEcgValue the previous ECG value (currently unused but kept
     *                     for potential future smoothing)
     * @return the synthesized ECG sample value
     */
    private double simulateEcgWaveform(int patientId, double lastEcgValue) {
        double hr = 60.0 + random.nextDouble() * 20.0;
        double t = System.currentTimeMillis() / 1000.0;
        double ecgFrequency = hr / 60.0;

        double pWave = 0.1 * Math.sin(2 * PI * ecgFrequency * t);
        double qrsComplex = 0.5 * Math.sin(2 * PI * 3 * ecgFrequency * t);
        double tWave = 0.2 * Math.sin(2 * PI * 2 * ecgFrequency * t + PI / 4);

        return pWave + qrsComplex + tWave + random.nextDouble() * 0.05;
    }
}
