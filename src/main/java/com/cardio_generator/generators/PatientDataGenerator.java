package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Common contract for all simulated patient data generators.
 *
 * <p>Each implementation simulates one type of cardiovascular signal — ECG,
 * blood pressure, blood saturation, blood levels, or nurse-call alerts — and
 * emits one or more measurements per call to {@link #generate}. Generators
 * are stateful per patient: most maintain a baseline or last-value array
 * indexed by patient ID so successive readings produce realistic trajectories
 * rather than purely random noise.
 *
 * <p>This interface is the abstraction behind the Strategy pattern as applied
 * to data <em>generation</em> (its sibling, {@link OutputStrategy}, applies
 * the same pattern to data <em>delivery</em>).
 *
 * @author 6439058
 */
public interface PatientDataGenerator {

    /**
     * Generates one round of simulated measurements for the given patient and
     * emits them through the supplied {@link OutputStrategy}.
     *
     * @param patientId      the ID of the patient to generate data for; must
     *                       match an index for which this generator was
     *                       initialized
     * @param outputStrategy the strategy used to deliver the generated values
     *                       to their destination; must not be null
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
