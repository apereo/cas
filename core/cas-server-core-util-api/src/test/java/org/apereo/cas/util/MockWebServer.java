package org.apereo.cas.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;


/**
 * Provides a simple HTTP Web server that can serve out a single resource for
 * all requests.  SSL/TLS is not supported.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Slf4j
public class MockWebServer implements AutoCloseable {
    /**
     * Request handler.
     */
    private final Worker worker;

    /**
     * Controls the worker thread.
     */
    private Thread workerThread;

    public MockWebServer(final int port) {
        try {
            this.worker = new Worker(new ServerSocket(port), null, MediaType.APPLICATION_JSON_VALUE);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot create Web server", e);
        }
    }

    public MockWebServer(final int port, final Resource resource, final HttpStatus status) {
        try {
            this.worker = new Worker(new ServerSocket(port), resource, status);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot create Web server", e);
        }
    }

    public MockWebServer(final int port, final Resource resource, final String contentType) {
        try {
            this.worker = new Worker(new ServerSocket(port), resource, contentType);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot create Web server", e);
        }
    }

    public MockWebServer(final int port, final String data) {
        try {
            this.worker = new Worker(new ServerSocket(port), new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8)), MediaType.APPLICATION_JSON_VALUE);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot create Web server", e);
        }
    }

    public MockWebServer(final int port, final Function<Socket, Object> funcExec) {
        try {
            this.worker = new Worker(new ServerSocket(port), funcExec);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot create Web server", e);
        }
    }

    /**
     * Starts the Web server so it can accept requests on the listening port.
     */
    public void start() {
        this.workerThread = new Thread(this.worker, "MockWebServer.Worker");
        this.workerThread.start();
    }

    /**
     * Stops the Web server after processing any pending requests.
     */
    public void stop() {
        if (!isRunning()) {
            return;
        }
        this.worker.stop();
        try {
            this.workerThread.join();
        } catch (final InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        stop();
    }

    /**
     * Determines whether the server is running or not.
     *
     * @return True if server is running, false otherwise.
     */
    public boolean isRunning() {
        return this.workerThread.isAlive();
    }

    /**
     * Worker class handles request processing.
     */
    private static class Worker implements Runnable {

        /**
         * Server always returns HTTP 200 response.
         */
        private static final String STATUS_LINE = "HTTP/1.1 %s %s\r%n";

        /**
         * Separates HTTP header from body.
         */
        private static final String SEPARATOR = "\r\n";

        /**
         * Response buffer size.
         */
        private static final int BUFFER_SIZE = 2048;
        private final ServerSocket serverSocket;
        private final Resource resource;
        private final String contentType;
        private final Function<Socket, Object> functionToExecute;
        private boolean running;
        private final HttpStatus status;

        Worker(final ServerSocket sock, final Resource resource, final String contentType) {
            this(sock, resource, contentType, HttpStatus.OK);
        }

        Worker(final ServerSocket sock, final Resource resource, final HttpStatus status) {
            this(sock, resource, MediaType.APPLICATION_JSON_VALUE, status);
        }

        Worker(final ServerSocket sock, final Resource resource, final String contentType, final HttpStatus status) {
            this.serverSocket = sock;
            this.resource = resource;
            this.contentType = contentType;
            this.functionToExecute = null;
            this.running = true;
            this.status = status;
        }

        Worker(final ServerSocket sock, final Function<Socket, Object> functionToExecute) {
            this.serverSocket = sock;
            this.functionToExecute = functionToExecute;
            this.resource = null;
            this.contentType = MediaType.APPLICATION_JSON_VALUE;
            this.running = true;
            this.status = HttpStatus.OK;
        }

        private static byte[] header(final String name, final Object value) {
            return String.format("%s: %s\r%n", name, value).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public synchronized void run() {
            while (this.running) {
                try {
                    val socket = this.serverSocket.accept();
                    if (this.functionToExecute != null) {
                        LOGGER.trace("Executed function with result [{}]", functionToExecute.apply(socket));
                    } else {
                        writeResponse(socket);
                    }
                    socket.shutdownOutput();
                    Thread.sleep(100);
                } catch (final SocketException e) {
                    LOGGER.debug("Stopping on socket close.");
                    this.running = false;
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        public void stop() {
            try {
                this.serverSocket.close();
            } catch (final IOException e) {
                LOGGER.trace("Exception when closing the server socket: [{}]", e.getMessage());
            }
        }

        private void writeResponse(final Socket socket) throws IOException {
            if (resource != null) {
                LOGGER.debug("Socket response for resource [{}]", resource.getFilename());
                val out = socket.getOutputStream();

                val statusLine = String.format(STATUS_LINE, status.value(), status.getReasonPhrase());
                out.write(statusLine.getBytes(StandardCharsets.UTF_8));
                out.write(header("Content-Length", this.resource.contentLength()));
                out.write(header("Content-Type", this.contentType));
                out.write(SEPARATOR.getBytes(StandardCharsets.UTF_8));

                val buffer = new byte[BUFFER_SIZE];
                try (val in = this.resource.getInputStream()) {
                    var count = 0;
                    while ((count = in.read(buffer)) > -1) {
                        out.write(buffer, 0, count);
                    }
                }
                LOGGER.debug("Wrote response for resource [{}] for [{}]", resource.getFilename(), resource.contentLength());
            }
        }
    }
}
