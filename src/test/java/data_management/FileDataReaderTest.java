package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class FileDataReaderTest {

    @Test
    void testParseSingleFile(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("HeartRate.txt");
        Files.writeString(file,
                "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 72.5\n"
                + "Patient ID: 1, Timestamp: 2000, Label: HeartRate, Data: 75.0\n");

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tmp.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals(72.5, records.get(0).getMeasurementValue());
    }

    @Test
    void testParseMultipleFiles(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("ECG.txt"),
                "Patient ID: 1, Timestamp: 1000, Label: ECG, Data: 0.42\n");
        Files.writeString(tmp.resolve("SystolicPressure.txt"),
                "Patient ID: 1, Timestamp: 2000, Label: SystolicPressure, Data: 120\n");

        DataStorage storage = new DataStorage();
        new FileDataReader(tmp.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
    }

    @Test
    void testParseSaturationWithPercentSign(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("Saturation.txt"),
                "Patient ID: 1, Timestamp: 1000, Label: Saturation, Data: 97.5%\n");

        DataStorage storage = new DataStorage();
        new FileDataReader(tmp.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(97.5, records.get(0).getMeasurementValue(), 1e-9);
    }

    @Test
    void testParseAlertTextValues(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("Alert.txt"),
                "Patient ID: 1, Timestamp: 1000, Label: Alert, Data: triggered\n"
                + "Patient ID: 1, Timestamp: 2000, Label: Alert, Data: resolved\n");

        DataStorage storage = new DataStorage();
        new FileDataReader(tmp.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals(1.0, records.get(0).getMeasurementValue());
        assertEquals(0.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testMalformedLineSkipped(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("X.txt"),
                "this line is garbage\n"
                + "Patient ID: 1, Timestamp: 1000, Label: X, Data: 42\n");

        DataStorage storage = new DataStorage();
        new FileDataReader(tmp.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size(), "Garbage line should be skipped, good line kept");
    }

    @Test
    void testMissingDirectoryThrows() {
        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader("/nonexistent/path/here");
        assertThrows(IOException.class, () -> {
            reader.readData(storage);
        });
    }
}
