package com.alerts.strategies;

import java.util.ArrayList;
import java.util.List;

import com.alerts.Alert;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.data_management.PatientRecord;

/**
 * Strategy that triggers blood-saturation alerts on:
 * <ol>
 *   <li><b>Low Saturation</b>: any reading below 92%.</li>
 *   <li><b>Rapid Drop</b>: a drop of 5% or more within a 10-minute window.</li>
 * </ol>
 *
 * @author 6439058
 */
public class OxygenSaturationStrategy implements AlertStrategy {

    private static final double LOW_THRESHOLD = 92.0;
    private static final double DROP_THRESHOLD = 5.0;
    private static final long TEN_MINUTES_MS = 10L * 60 * 1000;

    private final BloodOxygenAlertFactory factory = new BloodOxygenAlertFactory();

    @Override
    public Alert checkAlert(String patientId, List<PatientRecord> records) {
        List<PatientRecord> sat = filter(records, "Saturation");

        // Low saturation
        for (PatientRecord r : sat) {
            if (r.getMeasurementValue() < LOW_THRESHOLD) {
                return factory.createAlert(patientId, "LOW_SATURATION", r.getTimestamp());
            }
        }

        // Rapid drop within any 10-minute window
        for (int i = 0; i < sat.size(); i++) {
            PatientRecord ri = sat.get(i);
            for (int j = i + 1; j < sat.size(); j++) {
                PatientRecord rj = sat.get(j);
                if (rj.getTimestamp() - ri.getTimestamp() > TEN_MINUTES_MS) {
                    break; // beyond window, and records assumed time-ordered
                }
                if (ri.getMeasurementValue() - rj.getMeasurementValue() >= DROP_THRESHOLD) {
                    return factory.createAlert(patientId, "RAPID_DROP", rj.getTimestamp());
                }
            }
        }
        return null;
    }

    private List<PatientRecord> filter(List<PatientRecord> all, String type) {
        List<PatientRecord> out = new ArrayList<>();
        for (PatientRecord r : all) {
            if (type.equals(r.getRecordType())) {
                out.add(r);
            }
        }
        return out;
    }
}
