package com.data_management;

/**
 * Represents a single record of patient data at a specific point in time.
 *
 * <p>Each instance corresponds to one observation of one type of measurement
 * (e.g. an ECG sample, a systolic blood pressure reading) for one patient,
 * with the exact timestamp the measurement was taken.
 *
 * @author 6439058
 */
public class PatientRecord {
    private int patientId;
    private String recordType;
    private double measurementValue;
    private long timestamp;

    /**
     * Constructs a new patient record.
     *
     * @param patientId        the unique identifier for the patient
     * @param measurementValue the numerical value of the recorded measurement
     * @param recordType       the type of measurement (e.g. {@code "ECG"},
     *                         {@code "SystolicPressure"})
     * @param timestamp        epoch millis when the measurement was taken
     */
    public PatientRecord(int patientId, double measurementValue, String recordType, long timestamp) {
        this.patientId = patientId;
        this.measurementValue = measurementValue;
        this.recordType = recordType;
        this.timestamp = timestamp;
    }

    /** @return the patient ID associated with this record */
    public int getPatientId() {
        return patientId;
    }

    /** @return the numerical measurement value */
    public double getMeasurementValue() {
        return measurementValue;
    }

    /** @return epoch-millisecond timestamp when the measurement was recorded */
    public long getTimestamp() {
        return timestamp;
    }

    /** @return the record type (e.g. {@code "ECG"}, {@code "BloodPressure"}) */
    public String getRecordType() {
        return recordType;
    }
}
