/*
 * Copyright 2016-2018 Stefan Kalscheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stklcode.pubtrans.ura.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.stklcode.pubtrans.ura.model.Trip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Asynchronous stream reader foR URA stream API.
 * <p>
 * This reader provides a handler for asynchronous stream events.
 *
 * @author Stefan Kalscheuer
 * @since 1.2.0
 */
public class AsyncUraTripReader implements AutoCloseable {
    private static final Integer RES_TYPE_PREDICTION = 1;
    private static final Integer RES_TYPE_URA_VERSION = 4;

    private final List<Consumer<Trip>> consumers;
    private final URL url;
    private CompletableFuture<Void> future;
    private boolean cancelled;

    /**
     * Initialize trip reader.
     *
     * @param url       URL to read trips from.
     * @param consumers Initial list of consumers.
     */
    public AsyncUraTripReader(URL url, List<Consumer<Trip>> consumers) {
        this.url = url;
        this.consumers = new ArrayList<>(consumers);
    }

    public void open() {
        // Throw exeption, if future is already present.
        if (future != null) {
            throw new IllegalStateException("Reader already opened");
        }

        this.future = CompletableFuture.runAsync(() -> {
            ObjectMapper mapper = new ObjectMapper();

            try (InputStream is = url.openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String version = null;
                String line = br.readLine();
                while (line != null && !this.cancelled) {
                    List l = mapper.readValue(line, List.class);
                    // Check if result exists and has correct response type.
                    if (l != null && !l.isEmpty()) {
                        if (l.get(0).equals(RES_TYPE_URA_VERSION)) {
                            version = l.get(1).toString();
                        } else if (l.get(0).equals(RES_TYPE_PREDICTION)) {
                            // Parse Trip and pass to each consumer.
                            Trip trip = new Trip(l, version);
                            this.consumers.forEach(c -> c.accept(trip));
                        }
                    }
                    line = br.readLine();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read from API", e);
            }
        });
    }

    /**
     * Register an additional consumer.
     *
     * @param consumer New consumer.
     */
    public void addConsumer(Consumer<Trip> consumer) {
        consumers.add(consumer);
    }

    /**
     * Close the reader.
     * This is done by signaliung cancel to the asyncronous task. If the task is not completed
     * within 1 second however it is cancelled hard.
     */
    @Override
    public void close() {
        // Nothing to do if future is not yet started.
        if (future == null) {
            return;
        }

        // Signal cancelling to gracefully stop future.
        cancelled = true;
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read from API", e);
        } catch (TimeoutException e) {
            // Task failed to finish within 1 second.
            future.cancel(true);
        }
    }
}
