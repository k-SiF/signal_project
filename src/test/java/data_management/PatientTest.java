package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

class PatientTest {

    @Test
    void testAddAndGetAllRecords() {
        Patient p = new Patient(42);
        p.addRecord(98.6, "Temp", 1000L);
        p.addRecord(99.1, "Temp", 2000L);

        List<PatientRecord> all = p.getAllRecords();
        assertEquals(2, all.size());
        assertEquals(42, all.get(0).getPatientId());
    }

    @Test
    void testGetRecordsTimeRangeInclusive() {
        Patient p = new Patient(1);
        p.addRecord(10.0, "X", 100L);
        p.addRecord(20.0, "X", 200L);
        p.addRecord(30.0, "X", 300L);

        List<PatientRecord> r = p.getRecords(100L, 200L);
        assertEquals(2, r.size());
        assertEquals(10.0, r.get(0).getMeasurementValue());
        assertEquals(20.0, r.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecordsExcludesOutOfRange() {
        Patient p = new Patient(1);
        p.addRecord(10.0, "X", 100L);
        p.addRecord(20.0, "X", 200L);

        // Empty range
        assertTrue(p.getRecords(500L, 600L).isEmpty());
    }
}
