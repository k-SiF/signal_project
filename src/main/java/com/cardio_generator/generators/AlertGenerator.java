package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Simulates the nurse-call (panic-button) signal for a population of patients.
 *
 * <p>This generator does <strong>not</strong> derive alerts from vital signs;
 * it stochastically emits {@code "triggered"} and {@code "resolved"} events
 * to represent a patient pressing the call button at their bedside. State is
 * maintained per patient in {@link #alertStates}: a triggered alert resolves
 * itself with 90% probability on each subsequent call, while an untriggered
 * patient triggers a new alert with a probability derived from a Poisson
 * process with rate λ = 0.1 per period.
 *
 * <p>Distinct from {@code com.alerts.AlertGenerator}, which analyses stored
 * patient data to detect health-based alert conditions.
 *
 * @author 6439058
 */
public class AlertGenerator implements PatientDataGenerator {

    // Changed visibility from public to private (encapsulation).
    private static final Random randomGenerator = new Random();

    // Renamed from AlertStates to alertStates per Google Java Style §5.2.5:
    // field names must be lowerCamelCase, not UpperCamelCase.
    /** false = resolved, true = currently triggered. Indexed by patient ID. */
    private boolean[] alertStates;

    /**
     * Initializes the per-patient alert state array, sized to allow 1-based
     * indexing by patient ID.
     *
     * @param patientCount the number of patients to track
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Probabilistically updates the alert state for one patient and emits an
     * {@code "Alert"} event if the state changed.
     *
     * <p>If the patient currently has an active alert, there is a 90% chance
     * the alert resolves (emitting {@code "resolved"}). Otherwise, a new alert
     * triggers with probability {@code 1 - e^(-λ)} where λ=0.1, modelling a
     * Poisson arrival process (emitting {@code "triggered"}). When neither
     * transition fires, no output is produced.
     *
     * @param patientId      the 1-based ID of the patient to evaluate
     * @param outputStrategy the destination for any emitted alert event
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Renamed from Lambda to lambda per Google Java Style §5.2.5:
                // local variable names must be lowerCamelCase.
                double lambda = 0.1; // Average rate (alerts per period)
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
