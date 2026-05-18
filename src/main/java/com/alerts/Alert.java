package com.alerts;

/**
 * Encapsulates a single alert raised by the monitoring system.
 *
 * <p>An alert ties together a patient ID, a free-form condition string that
 * names the rule that fired (e.g. {@code "CRITICAL_HIGH_SYSTOLIC"}), and the
 * timestamp when the condition was detected.
 *
 * <p>Implements {@link IAlert} so it can be wrapped by decorators from the
 * {@code com.alerts.decorators} package (Week 4 Decorator pattern).
 *
 * @author 6439058
 */
public class Alert implements IAlert {
    private String patientId;
    private String condition;
    private long timestamp;

    /**
     * Constructs an alert.
     *
     * @param patientId patient identifier (as a String — the assignment spec
     *                  defines the factory signature this way)
     * @param condition free-form condition description
     * @param timestamp epoch millis when the alert was detected
     */
    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    @Override
    public String getPatientId() {
        return patientId;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
