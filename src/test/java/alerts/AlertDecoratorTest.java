package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.IAlert;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;

class AlertDecoratorTest {

    @Test
    void testPriorityDecoratorOverridesPriority() {
        Alert base = new Alert("1", "X", 1000L);
        assertEquals("MEDIUM", base.getPriority());

        IAlert decorated = new PriorityAlertDecorator(base, "HIGH");
        assertEquals("HIGH", decorated.getPriority());
    }

    @Test
    void testPriorityDecoratorAugmentsCondition() {
        Alert base = new Alert("1", "X", 1000L);
        IAlert decorated = new PriorityAlertDecorator(base, "HIGH");
        assertTrue(decorated.getCondition().startsWith("[HIGH]"));
    }

    @Test
    void testPriorityDecoratorPassesThroughOtherFields() {
        Alert base = new Alert("42", "X", 1234L);
        IAlert decorated = new PriorityAlertDecorator(base, "LOW");
        assertEquals("42", decorated.getPatientId());
        assertEquals(1234L, decorated.getTimestamp());
    }

    @Test
    void testRepeatedDecoratorAddsRepeatMarker() {
        Alert base = new Alert("1", "X", 1000L);
        IAlert decorated = new RepeatedAlertDecorator(base, 60);
        assertTrue(decorated.getCondition().contains("REPEAT:every=60s"));
    }

    @Test
    void testDecoratorsStack() {
        // Wrap a base alert with both decorators
        Alert base = new Alert("1", "CRITICAL_HIGH_SYSTOLIC", 1000L);
        IAlert decorated = new PriorityAlertDecorator(
                new RepeatedAlertDecorator(base, 30),
                "HIGH");

        // Priority is from outer decorator
        assertEquals("HIGH", decorated.getPriority());
        // Condition reflects both layers
        String cond = decorated.getCondition();
        assertTrue(cond.startsWith("[HIGH]"));
        assertTrue(cond.contains("REPEAT:every=30s"));
    }

    @Test
    void testNullWrappedThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new PriorityAlertDecorator(null, "HIGH"));
    }

    @Test
    void testInvalidPriorityRejected() {
        Alert base = new Alert("1", "X", 1000L);
        assertThrows(IllegalArgumentException.class,
                () -> new PriorityAlertDecorator(base, "SUPER_HIGH"));
    }
}
