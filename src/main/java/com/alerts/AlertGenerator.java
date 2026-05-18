package com.alerts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.strategies.AlertStrategy;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Analyzes patient data and emits alerts when health-rule criteria are met.
 *
 * <p>The class is the orchestrator for the Week 3 alert specification and the
 * integration point for the Week 4 design patterns:
 * <ul>
 *   <li>{@link AlertStrategy} implementations (Strategy pattern) encapsulate
 *       individual monitoring rules — blood pressure, oxygen saturation,
 *       heart rate.</li>
 *   <li>The strategies internally use {@code AlertFactory} subclasses
 *       (Factory Method pattern) to build correctly-prefixed {@link Alert}
 *       objects.</li>
 *   <li>This generator wraps urgent alerts in {@link PriorityAlertDecorator}
 *       (Decorator pattern) before passing them to {@link #triggerAlert}.</li>
 *   <li>The {@link com.alerts.decorators.RepeatedAlertDecorator} can be
 *       layered on by callers that want re-check semantics.</li>
 * </ul>
 *
 * <p>Combined alerts (Hypotensive Hypoxemia) and triggered (nurse-call)
 * alerts are evaluated directly in {@link #evaluateData} since they don't
 * fit cleanly under a single record-type strategy.
 *
 * @author 6439058
 */
public class AlertGenerator {

    private final DataStorage dataStorage;
    private final List<AlertStrategy> strategies;

    /**
     * In-memory record of alerts that have been triggered. Exposed via
     * {@link #getTriggeredAlerts()} so tests (and future routing layers)
     * can inspect what happened.
     */
    private final List<IAlert> triggered = new ArrayList<>();

    /**
     * Constructs the alert generator with the default strategy list (blood
     * pressure, oxygen saturation, heart rate).
     *
     * @param dataStorage source of patient records
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.strategies = new ArrayList<>();
        this.strategies.add(new BloodPressureStrategy());
        this.strategies.add(new OxygenSaturationStrategy());
        this.strategies.add(new HeartRateStrategy());
    }

    /**
     * Evaluates one patient's data against every registered strategy plus the
     * combined / triggered checks. Any resulting alerts are forwarded to
     * {@link #triggerAlert}.
     *
     * @param patient the patient to evaluate; if null, the method is a no-op
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            return;
        }
        List<PatientRecord> records = patient.getAllRecords();
        // Ensure chronological order so trend strategies can rely on it
        records.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        String patientId = String.valueOf(patient.getPatientId());

        // Run every strategy
        for (AlertStrategy strategy : strategies) {
            Alert alert = strategy.checkAlert(patientId, records);
            if (alert != null) {
                // Wrap critical alerts with HIGH priority via the Decorator pattern.
                String cond = alert.getCondition();
                if (cond.contains("CRITICAL") || cond.contains("LOW_SATURATION")
                        || cond.contains("RAPID_DROP") || cond.contains("ABNORMAL_PEAK")) {
                    triggerAlert(new PriorityAlertDecorator(alert, "HIGH"));
                } else {
                    triggerAlert(alert);
                }
            }
        }

        // Combined: Hypotensive Hypoxemia — low systolic AND low saturation
        checkHypotensiveHypoxemia(patientId, records);

        // Triggered alerts (from the simulator's nurse-call generator)
        checkTriggeredAlerts(patientId, records);
    }

    /** Combined alert: systolic < 90 AND saturation < 92 at any point. */
    private void checkHypotensiveHypoxemia(String patientId, List<PatientRecord> records) {
        boolean lowSys = false;
        boolean lowSat = false;
        long latestTs = 0;
        for (PatientRecord r : records) {
            if ("SystolicPressure".equals(r.getRecordType()) && r.getMeasurementValue() < 90.0) {
                lowSys = true;
                latestTs = Math.max(latestTs, r.getTimestamp());
            }
            if ("Saturation".equals(r.getRecordType()) && r.getMeasurementValue() < 92.0) {
                lowSat = true;
                latestTs = Math.max(latestTs, r.getTimestamp());
            }
        }
        if (lowSys && lowSat) {
            Alert combined = new Alert(patientId, "HYPOTENSIVE_HYPOXEMIA", latestTs);
            triggerAlert(new PriorityAlertDecorator(combined, "HIGH"));
        }
    }

    /**
     * Surfaces nurse-call (panic-button) events that arrived via the data
     * stream as {@code "Alert"} records with value 1.0 (triggered).
     */
    private void checkTriggeredAlerts(String patientId, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if ("Alert".equals(r.getRecordType()) && r.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(patientId, "MANUAL_TRIGGERED", r.getTimestamp()));
            }
        }
    }

    /**
     * Records an alert. In a production system this is where notifications
     * would be dispatched. Here we keep an in-memory list for inspection
     * by tests and downstream consumers.
     *
     * @param alert the alert to record
     */
    protected void triggerAlert(IAlert alert) {
        triggered.add(alert);
        System.out.println("ALERT [" + alert.getPriority() + "] patient=" + alert.getPatientId()
                + " condition=" + alert.getCondition() + " ts=" + alert.getTimestamp());
    }

    /**
     * Returns the list of alerts that have been triggered so far.
     * Used by tests; production code would consume alerts through a
     * different channel.
     *
     * @return an immutable snapshot of triggered alerts
     */
    public List<IAlert> getTriggeredAlerts() {
        return new ArrayList<>(triggered);
    }

    /** @return the underlying data storage this generator queries from */
    public DataStorage getDataStorage() {
        return dataStorage;
    }
}
