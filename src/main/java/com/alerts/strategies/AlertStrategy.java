package com.alerts.strategies;

import java.util.List;

import com.alerts.Alert;
import com.data_management.PatientRecord;

/**
 * Strategy contract for deciding whether a list of patient records warrants
 * an alert (Week 4 Strategy design pattern).
 *
 * <p>Each concrete strategy encapsulates one type of monitoring rule
 * (critical threshold, trend, drop-over-time, etc.). Strategies are
 * stateless and pure: they read records and return an alert if criteria
 * are met, otherwise {@code null}.
 *
 * @author 6439058
 */
public interface AlertStrategy {

    /**
     * Checks the given record list for the strategy's alert condition.
     *
     * @param patientId the patient ID (passed for inclusion in the resulting alert)
     * @param records   the patient's records the strategy should examine.
     *                  Filtering by record type is the strategy's responsibility.
     * @return an {@link Alert} if the condition is met, or {@code null} otherwise
     */
    Alert checkAlert(String patientId, List<PatientRecord> records);
}
