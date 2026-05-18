package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Streams generated patient data to a connected TCP client as CSV lines.
 *
 * <p>On construction, this strategy opens a {@link ServerSocket} on the
 * specified port and spawns a single background thread to wait for a client
 * connection. While no client is connected, calls to {@link #output} are
 * silently dropped (the data is not buffered). Once a client connects, each
 * subsequent output call writes one comma-separated line:
 * <pre>patientId,timestamp,label,data</pre>
 *
 * <p>This strategy supports exactly one client at a time. A reconnection
 * mechanism is not implemented; if the client disconnects, no further data
 * is delivered.
 *
 * @author 6439058
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Starts a TCP server on the given port and begins listening for one client.
     *
     * <p>The accept loop runs on a dedicated single-thread executor so this
     * constructor returns immediately, allowing the simulator to keep running
     * while waiting for a client to connect.
     *
     * @param port the TCP port the server will bind to; must be a valid,
     *             available port (1–65535)
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes one measurement as a CSV line to the connected TCP client.
     *
     * <p>If no client has connected yet (or the connection has been lost),
     * the call is silently dropped. The output format is:
     * <pre>patientId,timestamp,label,data</pre>
     *
     * @param patientId the patient identifier
     * @param timestamp epoch millis when the measurement was taken
     * @param label     the measurement type (e.g. {@code "ECG"})
     * @param data      the formatted measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
