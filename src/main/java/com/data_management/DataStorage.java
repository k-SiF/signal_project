package com.data_management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alerts.AlertGenerator;

/**
 * Central repository for all patient data within the monitoring system.
 *
 * <p>Implemented as a <b>Singleton</b> (Week 4): use {@link #getInstance()}
 * to obtain the shared instance. A public no-arg constructor is retained for
 * unit-testability so tests can build isolated storage instances without
 * touching global state.
 *
 * <p>The internal patient map uses {@link ConcurrentHashMap} so concurrent
 * inserts from a real-time {@code DataReader} (Week 5 WebSocket client) do
 * not race with reads from the alert engine.
 *
 * @author 6439058
 */
public class DataStorage {

    /** Singleton instance, lazily initialized. */
    private static DataStorage instance;

    /** Patient ID → Patient. Concurrent so reads and writes can interleave safely. */
    private Map<Integer, Patient> patientMap;

    /**
     * Public constructor — retained for testability.
     *
     * <p>Most application code should call {@link #getInstance()} instead so
     * everyone shares one storage instance. Tests should call this constructor
     * directly to obtain an isolated instance with no cross-test contamination.
     */
    public DataStorage() {
        this.patientMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the shared singleton instance, creating it on first call.
     *
     * @return the singleton {@code DataStorage}
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    /**
     * Adds a measurement to a patient's record list, creating the {@link Patient}
     * on the fly if no record exists yet for that patient ID.
     *
     * @param patientId        unique identifier of the patient
     * @param measurementValue the measured value
     * @param recordType       record type (e.g. {@code "HeartRate"})
     * @param timestamp        epoch millis when the measurement was taken
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            // Race-safe creation: putIfAbsent returns the existing value if
            // another thread inserted concurrently.
            Patient created = new Patient(patientId);
            Patient existing = ((ConcurrentHashMap<Integer, Patient>) patientMap).putIfAbsent(patientId, created);
            patient = (existing != null) ? existing : created;
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a patient's records within a closed time range.
     *
     * @param patientId unique identifier of the patient
     * @param startTime inclusive start (epoch millis)
     * @param endTime   inclusive end (epoch millis)
     * @return matching records, or an empty list if no patient exists
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>();
    }

    /**
     * Returns a snapshot list of all patients currently in storage.
     *
     * @return a new list containing all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * Demonstration entry point: loads any available data via a
     * {@link DataReader}, then runs the alert engine across all patients.
     *
     * <p>Argument 0, if present, is treated as a directory path for a
     * {@link FileDataReader}. Otherwise no data is loaded and the alert
     * engine just runs against the empty storage.
     *
     * @param args optional [0] = output directory produced by the simulator's
     *             {@code --output file:<dir>} mode
     */
    public static void main(String[] args) {
        DataStorage storage = DataStorage.getInstance();

        if (args.length > 0) {
            try {
                DataReader reader = new FileDataReader(args[0]);
                reader.readData(storage);
                System.out.println("Loaded data from " + args[0]);
            } catch (Exception e) {
                System.err.println("Could not load data: " + e.getMessage());
            }
        }

        for (Patient p : storage.getAllPatients()) {
            List<PatientRecord> records = p.getAllRecords();
            for (PatientRecord r : records) {
                System.out.println("Patient " + r.getPatientId()
                        + " | " + r.getRecordType()
                        + " = " + r.getMeasurementValue()
                        + " @ " + r.getTimestamp());
            }
        }

        AlertGenerator alertGenerator = new AlertGenerator(storage);
        for (Patient p : storage.getAllPatients()) {
            alertGenerator.evaluateData(p);
        }
    }
}
