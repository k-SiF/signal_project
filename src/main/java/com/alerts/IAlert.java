package com.alerts;

/**
 * Common contract for alert objects.
 *
 * <p>This interface exists primarily to support the <b>Decorator</b> design
 * pattern (Week 4). Both the concrete {@link Alert} class and the
 * {@code AlertDecorator} hierarchy in {@code com.alerts.decorators} implement
 * this interface, so decorators can wrap alerts (and even other decorators)
 * uniformly.
 *
 * @author 6439058
 */
public interface IAlert {
    /** @return the patient ID this alert pertains to */
    String getPatientId();

    /** @return a human-readable description of the alert condition */
    String getCondition();

    /** @return epoch millis when the alert was generated */
    long getTimestamp();

    /** @return the priority level: HIGH, MEDIUM, or LOW (default MEDIUM) */
    default String getPriority() {
        return "MEDIUM";
    }
}
