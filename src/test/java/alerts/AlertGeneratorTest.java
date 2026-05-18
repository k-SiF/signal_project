package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.alerts.IAlert;
import com.data_management.DataStorage;

import java.util.List;

class AlertGeneratorTest {

    /** Helper: feed records then run evaluation and return triggered alerts. */
    private List<IAlert> evaluateWith(int patientId, double[][] data, String[] types, long[] timestamps) {
        DataStorage storage = new DataStorage();
        for (int i = 0; i < data.length; i++) {
            storage.addPatientData(patientId, data[i][0], types[i], timestamps[i]);
        }
        AlertGenerator generator = new AlertGenerator(storage);
        generator.evaluateData(storage.getAllPatients().get(0));
        return generator.getTriggeredAlerts();
    }

    // ---------- Blood Pressure Critical Thresholds ----------

    @Test
    void testCriticalHighSystolicTriggers() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 185.0, "SystolicPressure", 1000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        List<IAlert> alerts = g.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("CRITICAL_HIGH_SYSTOLIC"));
        assertEquals("HIGH", alerts.get(0).getPriority(),
                "Critical alerts should be wrapped with HIGH priority via the Decorator pattern");
    }

    @Test
    void testCriticalLowSystolicTriggers() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 95.0, "Saturation", 1000L); // avoid hypotensive hypoxemia
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("CRITICAL_LOW_SYSTOLIC")));
    }

    @Test
    void testCriticalHighDiastolicTriggers() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 125.0, "DiastolicPressure", 1000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("CRITICAL_HIGH_DIASTOLIC")));
    }

    @Test
    void testCriticalLowDiastolicTriggers() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 55.0, "DiastolicPressure", 1000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("CRITICAL_LOW_DIASTOLIC")));
    }

    @Test
    void testNormalBloodPressureNoAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 80.0, "DiastolicPressure", 1000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().isEmpty());
    }

    // ---------- Blood Pressure Trends ----------

    @Test
    void testIncreasingSystolicTrendTriggers() {
        DataStorage storage = new DataStorage();
        // Each step > 10 mmHg: 110 -> 125 -> 140 (deltas 15, 15)
        storage.addPatientData(1, 110.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 140.0, "SystolicPressure", 3000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("INCREASING_TREND_SYSTOLIC")));
    }

    @Test
    void testDecreasingDiastolicTrendTriggers() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 110.0, "DiastolicPressure", 1000L);
        storage.addPatientData(1, 95.0, "DiastolicPressure", 2000L);
        storage.addPatientData(1, 80.0, "DiastolicPressure", 3000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("DECREASING_TREND_DIASTOLIC")));
    }

    @Test
    void testNonMonotonicNoTrendAlert() {
        DataStorage storage = new DataStorage();
        // Up, then down — not a monotonic trend
        storage.addPatientData(1, 110.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 3000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertFalse(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("TREND")));
    }

    @Test
    void testSmallChangesNoTrendAlert() {
        DataStorage storage = new DataStorage();
        // Increasing but each step <= 10 mmHg
        storage.addPatientData(1, 110.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 118.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 3000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertFalse(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("TREND")));
    }

    // ---------- Blood Saturation ----------

    @Test
    void testLowSaturationTriggers() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 88.0, "Saturation", 1000L);
        // Also add a normal systolic to avoid hypotensive hypoxemia confounding
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("LOW_SATURATION")));
    }

    @Test
    void testRapidDropTriggers() {
        DataStorage storage = new DataStorage();
        // Drop of 6% within 5 minutes (well inside 10-min window)
        storage.addPatientData(1, 98.0, "Saturation", 1_000_000L);
        storage.addPatientData(1, 92.0, "Saturation", 1_000_000L + 5 * 60 * 1000);
        // Both readings are >= 92, so won't trigger LOW_SATURATION
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("RAPID_DROP")
                            || a.getCondition().contains("LOW_SATURATION")));
    }

    @Test
    void testSlowDropNoAlert() {
        DataStorage storage = new DataStorage();
        // 6% drop, but over 20 minutes (outside 10-min window)
        storage.addPatientData(1, 98.0, "Saturation", 1_000_000L);
        storage.addPatientData(1, 92.0, "Saturation", 1_000_000L + 20 * 60 * 1000);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertFalse(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("RAPID_DROP")));
    }

    // ---------- Combined: Hypotensive Hypoxemia ----------

    @Test
    void testHypotensiveHypoxemiaTriggers() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 90.0, "Saturation", 2000L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("HYPOTENSIVE_HYPOXEMIA")
                            && "HIGH".equals(a.getPriority())));
    }

    @Test
    void testOnlyLowBPNoCombinedAlert() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 96.0, "Saturation", 2000L); // healthy
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertFalse(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("HYPOTENSIVE_HYPOXEMIA")));
    }

    // ---------- ECG Abnormal Peak ----------

    @Test
    void testEcgAbnormalPeakTriggers() {
        DataStorage storage = new DataStorage();
        // 10 small samples then one 10x spike
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 0.5, "ECG", 1000L + i);
        }
        storage.addPatientData(1, 5.0, "ECG", 1020L);
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("ABNORMAL_PEAK")));
    }

    @Test
    void testEcgNoPeakNoAlert() {
        DataStorage storage = new DataStorage();
        // 11 calm samples — all similar
        for (int i = 0; i < 11; i++) {
            storage.addPatientData(1, 0.5, "ECG", 1000L + i);
        }
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertFalse(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("ABNORMAL_PEAK")));
    }

    // ---------- Manual Triggered Alert ----------

    @Test
    void testManualTriggeredAlertSurfaces() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 1.0, "Alert", 1000L); // "triggered"
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertTrue(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("MANUAL_TRIGGERED")));
    }

    @Test
    void testResolvedAlertNotSurfaced() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 0.0, "Alert", 1000L); // "resolved"
        AlertGenerator g = new AlertGenerator(storage);
        g.evaluateData(storage.getAllPatients().get(0));

        assertFalse(g.getTriggeredAlerts().stream()
                .anyMatch(a -> a.getCondition().contains("MANUAL_TRIGGERED")));
    }

    // ---------- Null safety ----------

    @Test
    void testEvaluateNullPatientIsNoOp() {
        DataStorage storage = new DataStorage();
        AlertGenerator g = new AlertGenerator(storage);
        assertDoesNotThrow(() -> g.evaluateData(null));
        assertTrue(g.getTriggeredAlerts().isEmpty());
    }
}
