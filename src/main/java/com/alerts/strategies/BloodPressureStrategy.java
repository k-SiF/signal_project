package com.alerts.strategies;

import java.util.ArrayList;
import java.util.List;

import com.alerts.Alert;
import com.alerts.factories.BloodPressureAlertFactory;
import com.data_management.PatientRecord;

/**
 * Strategy that triggers blood-pressure alerts on either of two conditions
 * (Week 3 alert definitions):
 * <ol>
 *   <li><b>Critical Threshold</b>: systolic &gt; 180 or &lt; 90, or diastolic
 *       &gt; 120 or &lt; 60.</li>
 *   <li><b>Trend</b>: three consecutive readings (of the same type, in time
 *       order) where each one differs from the previous by more than 10 mmHg
 *       in the same direction (increasing or decreasing).</li>
 * </ol>
 *
 * <p>The strategy examines both {@code SystolicPressure} and
 * {@code DiastolicPressure} record types independently. The first matching
 * condition fires an alert — at most one alert is returned per call to
 * avoid spam from repeated checks of the same window.
 *
 * @author 6439058
 */
public class BloodPressureStrategy implements AlertStrategy {

    private static final double SYSTOLIC_HIGH = 180.0;
    private static final double SYSTOLIC_LOW = 90.0;
    private static final double DIASTOLIC_HIGH = 120.0;
    private static final double DIASTOLIC_LOW = 60.0;
    private static final double TREND_DELTA = 10.0;

    private final BloodPressureAlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public Alert checkAlert(String patientId, List<PatientRecord> records) {
        List<PatientRecord> systolic = filter(records, "SystolicPressure");
        List<PatientRecord> diastolic = filter(records, "DiastolicPressure");

        // Critical thresholds — systolic
        for (PatientRecord r : systolic) {
            if (r.getMeasurementValue() > SYSTOLIC_HIGH) {
                return factory.createAlert(patientId, "CRITICAL_HIGH_SYSTOLIC", r.getTimestamp());
            }
            if (r.getMeasurementValue() < SYSTOLIC_LOW) {
                return factory.createAlert(patientId, "CRITICAL_LOW_SYSTOLIC", r.getTimestamp());
            }
        }
        // Critical thresholds — diastolic
        for (PatientRecord r : diastolic) {
            if (r.getMeasurementValue() > DIASTOLIC_HIGH) {
                return factory.createAlert(patientId, "CRITICAL_HIGH_DIASTOLIC", r.getTimestamp());
            }
            if (r.getMeasurementValue() < DIASTOLIC_LOW) {
                return factory.createAlert(patientId, "CRITICAL_LOW_DIASTOLIC", r.getTimestamp());
            }
        }

        // Trend checks
        Alert systolicTrend = checkTrend(patientId, systolic, "SYSTOLIC");
        if (systolicTrend != null) return systolicTrend;
        Alert diastolicTrend = checkTrend(patientId, diastolic, "DIASTOLIC");
        if (diastolicTrend != null) return diastolicTrend;

        return null;
    }

    /**
     * Checks every sliding window of 3 records for a strict monotonic trend
     * where each step differs by more than {@link #TREND_DELTA}.
     */
    private Alert checkTrend(String patientId, List<PatientRecord> records, String prefix) {
        for (int i = 0; i + 2 < records.size(); i++) {
            double a = records.get(i).getMeasurementValue();
            double b = records.get(i + 1).getMeasurementValue();
            double c = records.get(i + 2).getMeasurementValue();
            if (b - a > TREND_DELTA && c - b > TREND_DELTA) {
                return factory.createAlert(patientId, "INCREASING_TREND_" + prefix,
                        records.get(i + 2).getTimestamp());
            }
            if (a - b > TREND_DELTA && b - c > TREND_DELTA) {
                return factory.createAlert(patientId, "DECREASING_TREND_" + prefix,
                        records.get(i + 2).getTimestamp());
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
