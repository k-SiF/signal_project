package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for blood-oxygen-saturation alerts.
 *
 * <p>Produces alerts whose {@code condition} string is prefixed with
 * {@code "BLOOD_OXYGEN:"}.
 *
 * @author 6439058
 */
public class BloodOxygenAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "BLOOD_OXYGEN:" + condition, timestamp);
    }
}
