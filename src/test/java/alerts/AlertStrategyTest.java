package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.strategies.AlertStrategy;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

class AlertStrategyTest {

    private List<PatientRecord> recs(double[][] values, String[] types, long[] ts) {
        List<PatientRecord> out = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            out.add(new PatientRecord(1, values[i][0], types[i], ts[i]));
        }
        return out;
    }

    @Test
    void testBloodPressureStrategyReturnsNullWhenSafe() {
        AlertStrategy s = new BloodPressureStrategy();
        List<PatientRecord> r = recs(
                new double[][] {{120}, {80}},
                new String[] {"SystolicPressure", "DiastolicPressure"},
                new long[] {1000L, 1000L});
        assertNull(s.checkAlert("1", r));
    }

    @Test
    void testBloodPressureStrategyFiresOnCriticalHigh() {
        AlertStrategy s = new BloodPressureStrategy();
        List<PatientRecord> r = recs(
                new double[][] {{190}},
                new String[] {"SystolicPressure"},
                new long[] {1000L});
        Alert a = s.checkAlert("1", r);
        assertNotNull(a);
        assertTrue(a.getCondition().contains("CRITICAL_HIGH_SYSTOLIC"));
    }

    @Test
    void testOxygenStrategyFiresBelow92() {
        AlertStrategy s = new OxygenSaturationStrategy();
        List<PatientRecord> r = recs(
                new double[][] {{91}},
                new String[] {"Saturation"},
                new long[] {1000L});
        Alert a = s.checkAlert("1", r);
        assertNotNull(a);
        assertTrue(a.getCondition().contains("LOW_SATURATION"));
    }

    @Test
    void testOxygenStrategyQuietWhenHealthy() {
        AlertStrategy s = new OxygenSaturationStrategy();
        List<PatientRecord> r = recs(
                new double[][] {{98}},
                new String[] {"Saturation"},
                new long[] {1000L});
        assertNull(s.checkAlert("1", r));
    }

    @Test
    void testHeartRateStrategyNoAlertWithFewSamples() {
        AlertStrategy s = new HeartRateStrategy();
        // Only 3 ECG samples — below the 10-sample window
        List<PatientRecord> r = recs(
                new double[][] {{0.5}, {0.5}, {0.5}},
                new String[] {"ECG", "ECG", "ECG"},
                new long[] {1L, 2L, 3L});
        assertNull(s.checkAlert("1", r));
    }

    @Test
    void testStrategiesAreDecoupled() {
        // Strategies don't depend on each other — each can be instantiated alone.
        assertDoesNotThrow(() -> new BloodPressureStrategy());
        assertDoesNotThrow(() -> new OxygenSaturationStrategy());
        assertDoesNotThrow(() -> new HeartRateStrategy());
    }
}
