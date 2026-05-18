package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;

class AlertFactoryTest {

    @Test
    void testBloodPressureFactoryPrefix() {
        AlertFactory f = new BloodPressureAlertFactory();
        Alert a = f.createAlert("42", "CRITICAL_HIGH_SYSTOLIC", 1000L);
        assertEquals("42", a.getPatientId());
        assertTrue(a.getCondition().startsWith("BLOOD_PRESSURE:"));
        assertEquals(1000L, a.getTimestamp());
    }

    @Test
    void testBloodOxygenFactoryPrefix() {
        AlertFactory f = new BloodOxygenAlertFactory();
        Alert a = f.createAlert("1", "LOW_SATURATION", 500L);
        assertTrue(a.getCondition().startsWith("BLOOD_OXYGEN:"));
    }

    @Test
    void testEcgFactoryPrefix() {
        AlertFactory f = new ECGAlertFactory();
        Alert a = f.createAlert("1", "ABNORMAL_PEAK", 500L);
        assertTrue(a.getCondition().startsWith("ECG:"));
    }

    @Test
    void testFactoriesPolymorphic() {
        // The whole point of Factory Method: same call shape, different products.
        AlertFactory[] factories = {
            new BloodPressureAlertFactory(),
            new BloodOxygenAlertFactory(),
            new ECGAlertFactory()
        };
        for (AlertFactory f : factories) {
            Alert a = f.createAlert("1", "X", 1L);
            assertNotNull(a);
        }
    }
}
