package cardio_generator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;

class SingletonTest {

    @Test
    void testHealthDataSimulatorSingleton() {
        HealthDataSimulator a = HealthDataSimulator.getInstance();
        HealthDataSimulator b = HealthDataSimulator.getInstance();
        assertSame(a, b);
    }

    @Test
    void testDataStorageSingleton() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }

    @Test
    void testDataStorageSingletonAndDirectInstanceAreDifferent() {
        // We deliberately retain a public constructor for testability.
        // The singleton instance and a directly constructed instance must
        // be separate objects so tests don't pollute the shared state.
        DataStorage shared = DataStorage.getInstance();
        DataStorage isolated = new DataStorage();
        assertNotSame(shared, isolated);
    }
}
