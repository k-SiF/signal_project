package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageTest {

    @Test
    void testAddAndGetRecords() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals(200.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecordsFiltersOutOfRange() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(1, 110.0, "HeartRate", 2000L);
        storage.addPatientData(1, 120.0, "HeartRate", 3000L);

        // Window [1500, 2500] should only include the 2000L record
        List<PatientRecord> mid = storage.getRecords(1, 1500L, 2500L);
        assertEquals(1, mid.size());
        assertEquals(110.0, mid.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsInclusiveBounds() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 50.0, "ECG", 1000L);
        storage.addPatientData(1, 60.0, "ECG", 2000L);

        // Both endpoints inclusive
        List<PatientRecord> exact = storage.getRecords(1, 1000L, 2000L);
        assertEquals(2, exact.size());

        // Zero-width range at exact timestamp still returns the record
        List<PatientRecord> point = storage.getRecords(1, 1000L, 1000L);
        assertEquals(1, point.size());
    }

    @Test
    void testGetRecordsUnknownPatientReturnsEmpty() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 50.0, "ECG", 1000L);

        List<PatientRecord> none = storage.getRecords(999, 0L, Long.MAX_VALUE);
        assertNotNull(none);
        assertTrue(none.isEmpty());
    }

    @Test
    void testGetAllPatientsReflectsInsertions() {
        DataStorage storage = new DataStorage();
        assertTrue(storage.getAllPatients().isEmpty());

        storage.addPatientData(1, 50.0, "ECG", 1000L);
        storage.addPatientData(2, 60.0, "ECG", 1000L);
        storage.addPatientData(1, 51.0, "ECG", 1100L); // existing patient, no duplicate

        List<Patient> all = storage.getAllPatients();
        assertEquals(2, all.size());
    }

    @Test
    void testSingletonReturnsSameInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b, "DataStorage.getInstance() should always return the same object");
    }
}
