package com.cardio_generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertGenerator;
import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Application entry point that orchestrates the simulation of cardiovascular
 * data for a configurable number of virtual patients.
 *
 * <p>Implemented as a <b>Singleton</b> (Week 4 design pattern requirement):
 * use {@link #getInstance()} rather than constructing directly. The
 * constructor is private so only one simulator instance can exist per JVM,
 * which is appropriate given that it owns a {@link ScheduledExecutorService}
 * and global output configuration.
 *
 * <p>The simulator parses command-line arguments to determine the patient
 * count and the output strategy ({@code console}, {@code file:<dir>},
 * {@code websocket:<port>}, or {@code tcp:<port>}), assigns each patient a
 * randomized integer ID, then schedules periodic data-generation tasks. Each
 * generator (ECG, blood pressure, blood saturation, blood levels, and
 * nurse-call alerts) runs at its own cadence.
 *
 * <p>Typical usage:
 * <pre>{@code
 * java -jar cardio_generator.jar --patient-count 50 --output file:./out
 * }</pre>
 *
 * @author 6439058
 */
public class HealthDataSimulator {

    /** Singleton instance — created lazily on first {@link #getInstance()} call. */
    private static HealthDataSimulator instance;

    private int patientCount = 50;
    private ScheduledExecutorService scheduler;
    private OutputStrategy outputStrategy = new ConsoleOutputStrategy();
    private final Random random = new Random();

    /** Private constructor — enforces Singleton: no external instantiation. */
    private HealthDataSimulator() {
    }

    /**
     * Returns the single instance of the simulator, creating it on first call.
     *
     * <p>The method is {@code synchronized} so two threads cannot race to
     * create two instances during the very first call.
     *
     * @return the singleton {@code HealthDataSimulator} instance
     */
    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    /**
     * Application entry point: delegates to the singleton instance.
     *
     * @param args command-line arguments. Supported flags:
     *             <ul>
     *               <li>{@code -h} — print help and exit</li>
     *               <li>{@code --patient-count <n>} — number of patients (default 50)</li>
     *               <li>{@code --output <type>} — one of {@code console},
     *                   {@code file:<dir>}, {@code websocket:<port>}, {@code tcp:<port>}</li>
     *             </ul>
     * @throws IOException if the file output directory cannot be created
     */
    public static void main(String[] args) throws IOException {
        HealthDataSimulator simulator = getInstance();
        simulator.run(args);
    }

    /**
     * Top-level orchestration: parse arguments, build the scheduler, then
     * schedule per-patient generation tasks.
     *
     * @param args raw command-line arguments
     * @throws IOException if the file output directory cannot be created
     */
    public void run(String[] args) throws IOException {
        parseArguments(args);

        scheduler = Executors.newScheduledThreadPool(patientCount * 4);

        List<Integer> patientIds = initializePatientIds(patientCount);
        Collections.shuffle(patientIds);

        scheduleTasksForPatients(patientIds);
    }

    /**
     * Reads the argument array and configures patient count and output strategy.
     * Calls {@link System#exit} with status 0 on {@code -h} or 1 on an unknown
     * option.
     *
     * @param args raw command-line arguments
     * @throws IOException if creating the file-output directory fails
     */
    private void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {
                        try {
                            patientCount = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid number of patients. Using default value: " + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                outputStrategy = new WebSocketOutputStrategy(port);
                                System.out.println("WebSocket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for WebSocket output. Please specify a valid port number.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                outputStrategy = new TcpOutputStrategy(port);
                                System.out.println("TCP socket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for TCP output. Please specify a valid port number.");
                            }
                        } else {
                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }

    /**
     * Prints CLI usage information to standard output, including all supported
     * flags and an example invocation.
     */
    private void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("Options:");
        System.out.println("  -h                       Show help and exit.");
        System.out.println("  --patient-count <count>  Specify the number of patients to simulate data for (default: 50).");
        System.out.println("  --output <type>          Define the output method. Options are:");
        System.out.println("                             'console' for console output,");
        System.out.println("                             'file:<directory>' for file output,");
        System.out.println("                             'websocket:<port>' for WebSocket output,");
        System.out.println("                             'tcp:<port>' for TCP socket output.");
        System.out.println("Example:");
        System.out.println("  java HealthDataSimulator --patient-count 100 --output websocket:8080");
    }

    /**
     * Builds a list of sequential patient IDs from 1 to {@code patientCount}.
     *
     * @param patientCount the number of patients to simulate
     * @return a mutable {@link List} containing the patient IDs 1..patientCount
     */
    private List<Integer> initializePatientIds(int patientCount) {
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }

    /**
     * For each patient, schedules five recurring data-generation tasks (ECG,
     * blood saturation, blood pressure, blood levels, and nurse-call alerts)
     * at their respective cadences on the shared scheduler.
     *
     * @param patientIds the shuffled list of patient IDs to schedule tasks for
     */
    private void scheduleTasksForPatients(List<Integer> patientIds) {
        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(patientCount);
        BloodSaturationDataGenerator bloodSaturationDataGenerator = new BloodSaturationDataGenerator(patientCount);
        BloodPressureDataGenerator bloodPressureDataGenerator = new BloodPressureDataGenerator(patientCount);
        BloodLevelsDataGenerator bloodLevelsDataGenerator = new BloodLevelsDataGenerator(patientCount);
        AlertGenerator alertGenerator = new AlertGenerator(patientCount);

        for (int patientId : patientIds) {
            scheduleTask(() -> ecgDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodSaturationDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodPressureDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.MINUTES);
            scheduleTask(() -> bloodLevelsDataGenerator.generate(patientId, outputStrategy), 2, TimeUnit.MINUTES);
            scheduleTask(() -> alertGenerator.generate(patientId, outputStrategy), 20, TimeUnit.SECONDS);
        }
    }

    /**
     * Schedules a {@link Runnable} on the shared scheduler with a random
     * initial delay (0–4 time units) and a fixed-rate period thereafter.
     *
     * @param task     the runnable to schedule
     * @param period   the interval between repeated executions
     * @param timeUnit the unit of {@code period}
     */
    private void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, random.nextInt(5), period, timeUnit);
    }
}
