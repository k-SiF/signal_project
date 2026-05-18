package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood-test values (cholesterol, white blood cells, red
 * blood cells) for a population of patients.
 *
 * <p>Each patient is assigned random baselines on construction. On each call
 * to {@link #generate}, three measurements are emitted with small random
 * deviations from those baselines: cholesterol (±5 mg/dL), white blood cells
 * (±0.5), red blood cells (±0.1).
 *
 * @author 6439058
 */
public class BloodLevelsDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private final double[] baselineCholesterol;
    private final double[] baselineWhiteCells;
    private final double[] baselineRedCells;

    /**
     * Constructs the generator with random baselines for each patient.
     *
     * @param patientCount number of patients to support
     */
    public BloodLevelsDataGenerator(int patientCount) {
        baselineCholesterol = new double[patientCount + 1];
        baselineWhiteCells = new double[patientCount + 1];
        baselineRedCells = new double[patientCount + 1];

        for (int i = 1; i <= patientCount; i++) {
            baselineCholesterol[i] = 150 + random.nextDouble() * 50;
            baselineWhiteCells[i] = 4 + random.nextDouble() * 6;
            baselineRedCells[i] = 4.5 + random.nextDouble() * 1.5;
        }
    }

    /**
     * Emits one set of three blood-test readings for the patient.
     *
     * @param patientId      patient identifier (1-based)
     * @param outputStrategy destination for the readings
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            double cholesterol = baselineCholesterol[patientId] + (random.nextDouble() - 0.5) * 10;
            double whiteCells = baselineWhiteCells[patientId] + (random.nextDouble() - 0.5) * 1;
            double redCells = baselineRedCells[patientId] + (random.nextDouble() - 0.5) * 0.2;

            outputStrategy.output(patientId, System.currentTimeMillis(), "Cholesterol", Double.toString(cholesterol));
            outputStrategy.output(patientId, System.currentTimeMillis(), "WhiteBloodCells", Double.toString(whiteCells));
            outputStrategy.output(patientId, System.currentTimeMillis(), "RedBloodCells", Double.toString(redCells));
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood levels data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
