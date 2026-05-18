package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for ECG-related alerts (irregular peaks, abnormal rhythms).
 *
 * <p>Produces alerts whose {@code condition} string is prefixed with
 * {@code "ECG:"}.
 *
 * @author 6439058
 */
public class ECGAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "ECG:" + condition, timestamp);
    }
}
