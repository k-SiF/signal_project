package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persists generated patient data to per-label files on disk.
 *
 * <p>For each unique measurement {@code label} (e.g. {@code "ECG"}), this
 * strategy creates a file named {@code <label>.txt} inside the configured
 * base directory and appends one line per measurement. File paths are cached
 * in a {@link java.util.concurrent.ConcurrentHashMap} so concurrent writes
 * from different generator threads do not race on path resolution.
 *
 * <p>Output line format:
 * <pre>Patient ID: &lt;id&gt;, Timestamp: &lt;ts&gt;, Label: &lt;label&gt;, Data: &lt;data&gt;</pre>
 *
 * @author 6439058
 */
public class FileOutputStrategy implements OutputStrategy {

    // Renamed from BaseDirectory to baseDirectory. Google Java Style §5.2.5:
    // field names must be lowerCamelCase.
    private String baseDirectory;

    // Renamed from file_map to fileMap. Google Java Style §5.2.5:
    // identifiers may not contain underscores.
    // Also changed visibility from public to private (encapsulation).
    private final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Constructs a strategy that writes all output beneath {@code baseDirectory}.
     * The directory is created lazily on the first call to {@link #output} if
     * it does not already exist.
     *
     * @param baseDirectory the absolute or relative directory path where output
     *                      files will be created; must not be null
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Appends one formatted line of data to the file corresponding to {@code label}.
     *
     * <p>On the first call for a given label, this method resolves the file path
     * (caching it in {@code fileMap}), creates the base directory if needed,
     * and opens the file in append mode. Subsequent calls re-use the cached
     * path. Errors during directory creation or writing are logged to
     * {@code System.err} but do not propagate, so a single I/O failure cannot
     * crash the simulator thread.
     *
     * @param patientId the patient identifier
     * @param timestamp epoch millis when the measurement was taken
     * @param label     the measurement type, also used to derive the file name
     * @param data      the formatted measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Renamed from FilePath to filePath. Google Java Style §5.2.5:
        // local variable names must be lowerCamelCase.
        String filePath = fileMap.computeIfAbsent(label,
                k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                    patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
