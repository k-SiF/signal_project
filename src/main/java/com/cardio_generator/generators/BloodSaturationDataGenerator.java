package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood-oxygen saturation (SpO₂) values for a population
 * of patients.
 *
 * <p>Each patient is initialized with a random baseline saturation between
 * 95% and 100% (the healthy range). On each call to {@link #generate}, the
 * current saturation drifts by ±1 from its last value, clamped to the
 * physiologically realistic range of 90%–100%. The previous value is
 * remembered per-patient so successive readings form a continuous walk
 * rather than independent samples.
 *
 * @author 6439058
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private int[] lastSaturationValues;

    /**
     * Initializes the per-patient baseline saturation array.
     *
     * @param patientCount the number of patients to simulate; the internal
     *                     array is sized {@code patientCount + 1} so patient
     *                     IDs can be used as 1-based indices
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // 95–100
        }
    }

    /**
     * Computes the next saturation reading for the patient (a small drift from
     * the previous value, clamped to 90–100%) and emits it through the
     * provided output strategy with the label {@code "Saturation"} and the
     * value formatted as {@code "<value>%"}.
     *
     * @param patientId      the 1-based ID of the patient to generate data for
     * @param outputStrategy the destination for the generated value
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            int variation = random.nextInt(3) - 1; // -1, 0, or 1
            int newSaturationValue = lastSaturationValues[patientId] + variation;
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
