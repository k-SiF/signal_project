package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated systolic and diastolic blood pressure readings.
 *
 * <p>Each patient is assigned random baseline systolic (110–130) and
 * diastolic (70–85) values on construction. Each call to {@link #generate}
 * produces a small random walk (±2) from the last value, clamped to a
 * realistic range (systolic 90–180, diastolic 60–120).
 *
 * @author 6439058
 */
public class BloodPressureDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();

    private int[] lastSystolicValues;
    private int[] lastDiastolicValues;

    /**
     * Constructs the generator with random baselines for each patient.
     *
     * @param patientCount number of patients to support
     */
    public BloodPressureDataGenerator(int patientCount) {
        lastSystolicValues = new int[patientCount + 1];
        lastDiastolicValues = new int[patientCount + 1];

        for (int i = 1; i <= patientCount; i++) {
            lastSystolicValues[i] = 110 + random.nextInt(20);
            lastDiastolicValues[i] = 70 + random.nextInt(15);
        }
    }

    /**
     * Emits one pair of blood pressure readings (systolic + diastolic).
     *
     * @param patientId      patient identifier (1-based)
     * @param outputStrategy destination for the readings
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            int systolicVariation = random.nextInt(5) - 2;
            int diastolicVariation = random.nextInt(5) - 2;
            int newSystolicValue = lastSystolicValues[patientId] + systolicVariation;
            int newDiastolicValue = lastDiastolicValues[patientId] + diastolicVariation;
            newSystolicValue = Math.min(Math.max(newSystolicValue, 90), 180);
            newDiastolicValue = Math.min(Math.max(newDiastolicValue, 60), 120);
            lastSystolicValues[patientId] = newSystolicValue;
            lastDiastolicValues[patientId] = newDiastolicValue;

            outputStrategy.output(patientId, System.currentTimeMillis(), "SystolicPressure",
                    Double.toString(newSystolicValue));
            outputStrategy.output(patientId, System.currentTimeMillis(), "DiastolicPressure",
                    Double.toString(newDiastolicValue));
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood pressure data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
