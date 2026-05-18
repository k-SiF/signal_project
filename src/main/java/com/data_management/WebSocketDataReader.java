package com.data_management;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Real-time {@link DataReader} that connects to a WebSocket server and
 * continuously deposits incoming records into a {@link DataStorage}.
 *
 * <p>The expected message format matches what {@code WebSocketOutputStrategy}
 * emits: comma-separated values:
 * <pre>patientId,timestamp,label,data</pre>
 *
 * <p>Robustness features:
 * <ul>
 *   <li>Malformed messages are logged and skipped — they cannot crash the
 *       receive loop.</li>
 *   <li>{@link #onClose} logs the disconnect; reconnection is the caller's
 *       responsibility (call {@link #startStreaming} again).</li>
 *   <li>The same {@code "Alert"} text mapping as {@link FileDataReader}
 *       applies: {@code triggered} → 1.0, {@code resolved} → 0.0.</li>
 * </ul>
 *
 * <p>Typical usage:
 * <pre>{@code
 * DataStorage storage = DataStorage.getInstance();
 * WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:8080");
 * reader.startStreaming(storage);
 * // ... data flows in ...
 * reader.stopStreaming();
 * }</pre>
 *
 * @author 6439058
 */
public class WebSocketDataReader implements DataReader {

    private final URI serverUri;
    private InternalClient client;

    /**
     * Constructs a reader that will connect to the given WebSocket URI.
     *
     * @param serverUri a URI of the form {@code "ws://host:port"}
     * @throws IllegalArgumentException if the URI is malformed
     */
    public WebSocketDataReader(String serverUri) {
        try {
            this.serverUri = new URI(serverUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad WebSocket URI: " + serverUri, e);
        }
    }

    /**
     * Not used in streaming mode. Throws to make accidental misuse loud.
     *
     * @param dataStorage ignored
     * @throws IOException always — call {@link #startStreaming} instead
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        throw new IOException("WebSocketDataReader is a streaming reader; "
                + "call startStreaming() instead of readData().");
    }

    /**
     * Connects to the WebSocket server and begins delivering messages to
     * the provided storage. Returns once the connection is established
     * (or fails) — message handling continues asynchronously on the
     * Java-WebSocket library's thread.
     *
     * @param dataStorage destination for incoming records
     * @throws IOException if the connection cannot be opened
     */
    @Override
    public void startStreaming(DataStorage dataStorage) throws IOException {
        client = new InternalClient(serverUri, dataStorage);
        try {
            boolean connected = client.connectBlocking();
            if (!connected) {
                throw new IOException("Failed to connect to " + serverUri);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while connecting to " + serverUri, e);
        }
    }

    /** Closes the WebSocket connection and stops receiving messages. */
    @Override
    public void stopStreaming() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    /**
     * Parses one incoming CSV message and stores the resulting record.
     * Public for testability — tests can hit it directly without a real socket.
     *
     * @param message     the raw CSV message
     * @param dataStorage destination for the parsed record
     */
    public static void parseAndStore(String message, DataStorage dataStorage) {
        if (message == null || message.isEmpty()) {
            return;
        }
        String[] parts = message.split(",", 4);
        if (parts.length != 4) {
            System.err.println("Skipping malformed message (expected 4 fields): " + message);
            return;
        }
        try {
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label = parts[2].trim();
            String dataStr = parts[3].trim();

            double value;
            if ("Alert".equals(label)) {
                value = "triggered".equalsIgnoreCase(dataStr) ? 1.0 : 0.0;
            } else if (dataStr.endsWith("%")) {
                value = Double.parseDouble(dataStr.substring(0, dataStr.length() - 1));
            } else {
                value = Double.parseDouble(dataStr);
            }
            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            System.err.println("Skipping unparseable message: " + message + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Inner subclass of the Java-WebSocket {@link WebSocketClient} that
     * forwards onMessage callbacks to {@link #parseAndStore}.
     */
    private static class InternalClient extends WebSocketClient {
        private final DataStorage dataStorage;

        InternalClient(URI uri, DataStorage dataStorage) {
            super(uri);
            this.dataStorage = dataStorage;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("WebSocket connected to " + getURI());
        }

        @Override
        public void onMessage(String message) {
            parseAndStore(message, dataStorage);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("WebSocket closed (code=" + code + ", remote=" + remote + "): " + reason);
        }

        @Override
        public void onError(Exception ex) {
            System.err.println("WebSocket error: " + ex.getMessage());
        }
    }
}
