package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for blood-pressure-related alerts.
 *
 * <p>Produces alerts whose {@code condition} string is prefixed with
 * {@code "BLOOD_PRESSURE:"} so routing logic can dispatch them as a family.
 *
 * @author 6439058
 */
public class BloodPressureAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "BLOOD_PRESSURE:" + condition, timestamp);
    }
}
