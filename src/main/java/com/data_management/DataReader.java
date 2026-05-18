package com.data_management;

import java.io.IOException;

/**
 * Contract for components that ingest patient data into a {@link DataStorage}.
 *
 * <p>Two styles of reader are supported:
 * <ul>
 *   <li><b>Batch readers</b> (e.g. file-based) call {@link #readData}, do all
 *       their work synchronously, and return when finished. Week 3 behaviour.</li>
 *   <li><b>Streaming readers</b> (e.g. WebSocket-based) override
 *       {@link #startStreaming} to begin asynchronous, continuous ingestion
 *       and {@link #stopStreaming} to halt it. Week 5 behaviour.</li>
 * </ul>
 *
 * <p>An implementation may support either style; methods it does not use
 * have safe default no-op implementations.
 *
 * @author 6439058
 */
public interface DataReader {

    /**
     * Reads data from a source and stores it in the provided data storage.
     * The semantics of "data source" are left to the implementation
     * (a directory of files, an HTTP endpoint, etc.).
     *
     * @param dataStorage the storage where data will be deposited
     * @throws IOException if there is an error reading the data
     */
    void readData(DataStorage dataStorage) throws IOException;

    /**
     * Begins asynchronous, continuous ingestion into the given storage.
     * Default implementation is a no-op for batch readers.
     *
     * @param dataStorage the storage where streaming data will be deposited
     * @throws IOException if the stream cannot be started
     */
    default void startStreaming(DataStorage dataStorage) throws IOException {
        // No-op default for batch readers
    }

    /**
     * Stops any in-progress streaming ingestion and releases resources.
     * Default implementation is a no-op for batch readers.
     */
    default void stopStreaming() {
        // No-op default for batch readers
    }
}
