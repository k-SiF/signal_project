package com.alerts.strategies;

import java.util.ArrayList;
import java.util.List;

import com.alerts.Alert;
import com.alerts.factories.ECGAlertFactory;
import com.data_management.PatientRecord;

/**
 * Strategy that triggers an ECG alert when a peak occurs far above the
 * current sliding-window average.
 *
 * <p>Implementation: maintain a window of the last {@code WINDOW_SIZE} ECG
 * samples; if a new sample exceeds the window mean by more than
 * {@code PEAK_MULTIPLIER}× the window mean (and the mean is positive), an
 * alert fires.
 *
 * <p>The window size is small enough to react quickly to changing baselines
 * but large enough to filter out single-sample noise.
 *
 * @author 6439058
 */
public class HeartRateStrategy implements AlertStrategy {

    private static final int WINDOW_SIZE = 10;
    private static final double PEAK_MULTIPLIER = 3.0;

    private final ECGAlertFactory factory = new ECGAlertFactory();

    @Override
    public Alert checkAlert(String patientId, List<PatientRecord> records) {
        List<PatientRecord> ecg = filter(records, "ECG");
        if (ecg.size() < WINDOW_SIZE + 1) {
            return null;
        }

        for (int i = WINDOW_SIZE; i < ecg.size(); i++) {
            double sum = 0;
            for (int j = i - WINDOW_SIZE; j < i; j++) {
                sum += Math.abs(ecg.get(j).getMeasurementValue());
            }
            double mean = sum / WINDOW_SIZE;
            double current = Math.abs(ecg.get(i).getMeasurementValue());
            if (mean > 0 && current > PEAK_MULTIPLIER * mean) {
                return factory.createAlert(patientId, "ABNORMAL_PEAK", ecg.get(i).getTimestamp());
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
