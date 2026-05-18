package com.data_management;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Reads patient data from a directory of text files produced by
 * {@code FileOutputStrategy}.
 *
 * <p>The simulator's file output strategy creates one file per measurement
 * label (e.g. {@code ECG.txt}, {@code SystolicPressure.txt}) with lines in
 * the format:
 * <pre>Patient ID: 5, Timestamp: 1714376789050, Label: ECG, Data: 0.42</pre>
 *
 * <p>This reader walks the configured directory, parses every line that
 * matches that format, and feeds each measurement into the provided
 * {@link DataStorage}. Lines that don't parse (blank lines, garbled records)
 * are skipped with a warning rather than aborting the whole read.
 *
 * <p>The {@code "Alert"} label is parsed specially: its data is the string
 * {@code "triggered"} or {@code "resolved"} (not a number), so the value is
 * stored as {@code 1.0} for triggered or {@code 0.0} for resolved to fit
 * the numeric {@link PatientRecord#getMeasurementValue()} field.
 *
 * @author 6439058
 */
public class FileDataReader implements DataReader {

    private final String directoryPath;

    /**
     * Constructs a reader pointing at a directory of output files.
     *
     * @param directoryPath absolute or relative path to the directory
     *                      containing output files produced by
     *                      {@code FileOutputStrategy}
     */
    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Walks the directory and parses every {@code .txt} file found.
     *
     * @param dataStorage the destination for parsed records
     * @throws IOException if the directory cannot be opened or read
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (!Files.isDirectory(dir)) {
            throw new IOException("Not a directory: " + directoryPath);
        }
        try (Stream<Path> entries = Files.list(dir)) {
            for (Path entry : (Iterable<Path>) entries::iterator) {
                if (Files.isRegularFile(entry) && entry.toString().endsWith(".txt")) {
                    parseFile(entry, dataStorage);
                }
            }
        }
    }

    /**
     * Parses one output file line by line, dispatching valid records to storage.
     *
     * @param file        the file to parse
     * @param dataStorage destination for parsed records
     * @throws IOException if the file cannot be read
     */
    private void parseFile(Path file, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    parseLine(line, dataStorage);
                } catch (Exception e) {
                    System.err.println("Skipping malformed line " + lineNo
                            + " in " + file.getFileName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses one line of output and inserts the resulting record into storage.
     *
     * <p>Expected format:
     * <pre>Patient ID: &lt;int&gt;, Timestamp: &lt;long&gt;, Label: &lt;str&gt;, Data: &lt;value&gt;</pre>
     *
     * @param line        the raw line of text
     * @param dataStorage destination for the parsed record
     */
    private void parseLine(String line, DataStorage dataStorage) {
        // Split on ", " (with the space) — labels may contain spaces but the
        // commas separating fields are followed by " ", and field values
        // themselves don't contain that exact sequence.
        String[] parts = line.split(", ");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Expected 4 fields, found " + parts.length);
        }

        int patientId = Integer.parseInt(stripPrefix(parts[0], "Patient ID:"));
        long timestamp = Long.parseLong(stripPrefix(parts[1], "Timestamp:"));
        String label = stripPrefix(parts[2], "Label:");
        String dataStr = stripPrefix(parts[3], "Data:");

        double value;
        if ("Alert".equals(label)) {
            value = "triggered".equalsIgnoreCase(dataStr) ? 1.0 : 0.0;
        } else if (dataStr.endsWith("%")) {
            // BloodSaturation values are written like "97.0%"
            value = Double.parseDouble(dataStr.substring(0, dataStr.length() - 1));
        } else {
            value = Double.parseDouble(dataStr);
        }

        dataStorage.addPatientData(patientId, value, label, timestamp);
    }

    /**
     * Strips a leading prefix and trims whitespace.
     *
     * @param s      the input string
     * @param prefix the prefix to strip
     * @return the value portion, trimmed
     */
    private String stripPrefix(String s, String prefix) {
        s = s.trim();
        if (!s.startsWith(prefix)) {
            throw new IllegalArgumentException("Missing prefix '" + prefix + "' in: " + s);
        }
        return s.substring(prefix.length()).trim();
    }
}
