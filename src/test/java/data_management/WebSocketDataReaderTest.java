package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;

import java.io.IOException;
import java.util.List;

class WebSocketDataReaderTest {

    @Test
    void testParseAndStoreValidMessage() {
        DataStorage storage = new DataStorage();
        WebSocketDataReader.parseAndStore("1,1000,HeartRate,72.5", storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(72.5, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(1000L, records.get(0).getTimestamp());
    }

    @Test
    void testParseSaturationWithPercent() {
        DataStorage storage = new DataStorage();
        WebSocketDataReader.parseAndStore("1,1000,Saturation,97.0%", storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(97.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testParseAlertTextValues() {
        DataStorage storage = new DataStorage();
        WebSocketDataReader.parseAndStore("1,1000,Alert,triggered", storage);
        WebSocketDataReader.parseAndStore("1,2000,Alert,resolved", storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals(1.0, records.get(0).getMeasurementValue());
        assertEquals(0.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testMalformedMessageSkippedNotThrown() {
        DataStorage storage = new DataStorage();
        // Too few fields
        assertDoesNotThrow(() -> WebSocketDataReader.parseAndStore("garbage", storage));
        // Non-numeric ID
        assertDoesNotThrow(() -> WebSocketDataReader.parseAndStore("X,1000,L,5.0", storage));
        // Storage should have received nothing
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testEmptyMessageIgnored() {
        DataStorage storage = new DataStorage();
        WebSocketDataReader.parseAndStore("", storage);
        WebSocketDataReader.parseAndStore(null, storage);
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testReadDataThrowsBecauseStreamingReader() {
        WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:9999");
        DataStorage storage = new DataStorage();
        // readData should refuse — this is a streaming reader, not batch
        assertThrows(IOException.class, () -> {
            reader.readData(storage);
        });
    }

    @Test
    void testBadUriRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new WebSocketDataReader("not a valid uri ::: "));
    }
}
