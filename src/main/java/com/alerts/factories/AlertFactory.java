package com.alerts.factories;

import com.alerts.Alert;

/**
 * Abstract base for the <b>Factory Method</b> design pattern applied to alert
 * creation (Week 4).
 *
 * <p>Concrete subclasses override {@link #createAlert} to produce a fully-formed
 * {@link Alert} whose {@code condition} string is prefixed with the alert
 * family (e.g. {@code "BLOOD_PRESSURE:CRITICAL_HIGH_SYSTOLIC"}). This lets
 * downstream consumers (logging, routing, decorating) distinguish alert
 * families without parsing free-form text.
 *
 * <p>Using subclasses (rather than a switch statement) means new alert
 * families can be introduced without touching existing factory code —
 * Open/Closed Principle.
 *
 * @author 6439058
 */
public abstract class AlertFactory {

    /**
     * Creates an {@link Alert} appropriate to this factory's family.
     *
     * @param patientId patient identifier
     * @param condition the specific condition that triggered the alert
     *                  (factory-specific format, e.g.
     *                  {@code "CRITICAL_HIGH_SYSTOLIC"})
     * @param timestamp epoch millis when the alert was detected
     * @return a new {@link Alert} instance
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
