package com.data_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single patient and manages their medical records.
 *
 * <p>Holds a list of {@link PatientRecord} instances in insertion order and
 * provides time-range retrieval. Records may interleave different
 * measurement types (ECG, BloodPressure, etc.) — filtering by type is the
 * caller's responsibility.
 *
 * @author 6439058
 */
public class Patient {
    private int patientId;
    private List<PatientRecord> patientRecords;

    /**
     * Constructs a new Patient with the given ID and an empty record list.
     *
     * @param patientId the unique identifier for the patient
     */
    public Patient(int patientId) {
        this.patientId = patientId;
        this.patientRecords = new ArrayList<>();
    }

    /** @return the unique ID of this patient */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Appends a new record for this patient.
     *
     * @param measurementValue the measurement value
     * @param recordType       the type of measurement (e.g. {@code "ECG"})
     * @param timestamp        epoch millis when the measurement was taken
     */
    public void addRecord(double measurementValue, String recordType, long timestamp) {
        PatientRecord record = new PatientRecord(this.patientId, measurementValue, recordType, timestamp);
        this.patientRecords.add(record);
    }

    /**
     * Returns all records for this patient whose timestamp falls within the
     * inclusive range {@code [startTime, endTime]}.
     *
     * <p>The bounds are inclusive on both ends so a zero-width query
     * ({@code startTime == endTime}) still returns records at exactly that
     * timestamp, which is the principle-of-least-surprise interpretation.
     *
     * @param startTime inclusive start of the range (epoch millis)
     * @param endTime   inclusive end of the range (epoch millis)
     * @return a new list of matching records; never null, may be empty
     */
    public List<PatientRecord> getRecords(long startTime, long endTime) {
        List<PatientRecord> filteredRecords = new ArrayList<>();
        for (PatientRecord record : patientRecords) {
            long ts = record.getTimestamp();
            if (ts >= startTime && ts <= endTime) {
                filteredRecords.add(record);
            }
        }
        return filteredRecords;
    }

    /**
     * Returns all records for this patient, unfiltered.
     * Used by the alert evaluation logic that needs the full history.
     *
     * @return a new list of all records; never null
     */
    public List<PatientRecord> getAllRecords() {
        return new ArrayList<>(patientRecords);
    }
}
