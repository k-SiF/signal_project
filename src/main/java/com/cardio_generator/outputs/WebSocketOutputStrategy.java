package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * Streams generated patient data to all connected WebSocket clients as CSV.
 *
 * <p>On construction this strategy starts a {@link WebSocketServer} bound to
 * the given port. Any number of clients can connect; each call to
 * {@link #output} broadcasts the same comma-separated message to every
 * currently-connected client:
 * <pre>patientId,timestamp,label,data</pre>
 *
 * <p>The internal {@code SimpleWebSocketServer} subclass only logs connection
 * lifecycle events — incoming messages from clients are ignored, since this
 * strategy is one-way (server → clients).
 *
 * @author 6439058
 */
public class WebSocketOutputStrategy implements OutputStrategy {

    private WebSocketServer server;

    /**
     * Starts a WebSocket server on the given port and begins listening for
     * client connections.
     *
     * @param port the TCP port the WebSocket server will bind to; must be
     *             available
     */
    public WebSocketOutputStrategy(int port) {
        server = new SimpleWebSocketServer(new InetSocketAddress(port));
        System.out.println("WebSocket server created on port: " + port
                + ", listening for connections...");
        server.start();
    }

    /**
     * Broadcasts one measurement as a CSV line to every connected client.
     * Clients that join after this call will not receive past messages
     * (no buffering).
     *
     * @param patientId the patient identifier
     * @param timestamp epoch millis when the measurement was taken
     * @param label     the measurement type (e.g. {@code "ECG"})
     * @param data      the formatted measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
        for (WebSocket conn : server.getConnections()) {
            conn.send(message);
        }
    }

    /**
     * Minimal {@link WebSocketServer} subclass that logs lifecycle events and
     * ignores any incoming messages from clients (this strategy is one-way).
     */
    private static class SimpleWebSocketServer extends WebSocketServer {

        public SimpleWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // Not used: this strategy is one-way (server -> clients).
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            System.out.println("Server started successfully");
        }
    }
}
