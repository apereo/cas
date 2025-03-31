package org.apereo.cas.util;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.cryptacular.io.ClassPathResource;
import org.jooq.lambda.Unchecked;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Provides a simple HTTP Web server that can serve out a single resource for
 * all requests.  SSL/TLS is not supported.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Slf4j
public class MockWebServer implements Closeable {
    private static final int STARTING_PORT_RANGE = 4000;
    private static final int ENDING_PORT_RANGE = 9999;

    private static final ConcurrentSkipListSet ALL_PORTS = new ConcurrentSkipListSet<>();
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final Worker worker;

    private Thread workerThread;

    public MockWebServer(final boolean ssl) {
        try {
            this.worker = new Worker(getServerSocket(getRandomPort(), ssl),
                new ByteArrayResource("OK".getBytes(StandardCharsets.UTF_8)), MediaType.APPLICATION_JSON_VALUE, HttpStatus.OK);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server", e);
        }
    }
    
    public MockWebServer(final boolean ssl, final int port, final Resource resource) {
        try {
            this.worker = new Worker(getServerSocket(port, ssl), resource, MediaType.APPLICATION_JSON_VALUE, HttpStatus.OK);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final boolean ssl, final String resource) {
        val port = getRandomPort();
        try {
            this.worker = new Worker(getServerSocket(port, ssl),
                new ByteArrayResource(resource.getBytes(StandardCharsets.UTF_8)), MediaType.APPLICATION_JSON_VALUE, HttpStatus.OK);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final boolean ssl, final Resource resource) {
        val port = getRandomPort();
        try {
            this.worker = new Worker(getServerSocket(port, ssl), resource, MediaType.APPLICATION_JSON_VALUE, HttpStatus.OK);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer() {
        this(HttpStatus.OK);
    }

    public MockWebServer(final HttpStatus status) {
        this(getRandomPort(), status);
    }

    public MockWebServer(final int port, final String resource, final HttpStatus status) {
        this(port, new ByteArrayResource(resource.getBytes(StandardCharsets.UTF_8), "Output"), status);
    }

    public MockWebServer(final String resource, final HttpStatus status) {
        this(getRandomPort(), new ByteArrayResource(resource.getBytes(StandardCharsets.UTF_8), "Output"),
            MediaType.APPLICATION_JSON_VALUE, status);
    }


    public MockWebServer(final Resource resource, final String contentType) {
        this(getRandomPort(), resource, contentType, HttpStatus.OK);
    }

    public MockWebServer(final Resource resource) {
        this(resource, MediaType.APPLICATION_JSON_VALUE);
    }

    public MockWebServer(final String jsonResource) {
        this(new ByteArrayResource(jsonResource.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE);
    }

    public MockWebServer(final int port) {
        try {
            this.worker = new Worker(getServerSocket(port, false), MediaType.APPLICATION_JSON_VALUE);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final int port, final Object body) {
        try {
            val data = toJsonString(body);
            val resource = new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output");
            this.worker = new Worker(getServerSocket(port, false), resource,
                HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE, Map.of());
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final Map body) {
        this(toJsonString(body));
    }

    public MockWebServer(final int port, final Object body, final Map headers, final HttpStatus status) {
        try {
            val data = toJsonString(body);
            val resource = new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output");
            this.worker = new Worker(getServerSocket(port, false), resource, status, MediaType.APPLICATION_JSON_VALUE, headers);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final int port, final HttpStatus status) {
        this(port, new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), status);
    }

    public MockWebServer(final int port, final Resource resource, final String contentType) {
        this(port, resource, contentType, HttpStatus.OK);
    }

    public MockWebServer(final int port, final Resource resource, final HttpStatus status) {
        try {
            this.worker = new Worker(getServerSocket(port, false), resource, status);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final int port, final Resource resource, final String contentType, final HttpStatus status) {
        try {
            this.worker = new Worker(getServerSocket(port, false), resource, contentType, status);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final int port, final String data) {
        try {
            this.worker = new Worker(getServerSocket(port, false), MediaType.APPLICATION_JSON_VALUE);
            responseBody(data);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public MockWebServer(final int port, final Function<Socket, Object> funcExec) {
        try {
            this.worker = new Worker(getServerSocket(port, false), funcExec);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot create Web server on port " + port, e);
        }
    }

    public void responseStatus(final HttpStatus httpStatus) {
        this.worker.setStatus(httpStatus);
    }

    public void setContentType(final String value) {
        this.worker.setContentType(value);
    }

    private static ServerSocket getServerSocket(final int port, final boolean ssl) throws Exception {
        if (port == 8443 || ssl) {
            val sslContext = SSLContext.getInstance("SSL");
            var keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ClassPathResource("localhost.keystore").getInputStream(), "changeit".toCharArray());
            val keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "changeit".toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] xcs, final String string) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] xcs, final String string) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, RandomUtils.getNativeInstance());
            val addr = new InetSocketAddress("0.0.0.0", port);
            return sslContext.getServerSocketFactory().createServerSocket(port, 0, addr.getAddress());
        }
        val serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(30000);
        return serverSocket;
    }

    public int getPort() {
        return this.worker.getPort();
    }

    public void responseBody(final String data) {
        this.worker.setResource(new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8)));
        this.worker.setResourceSupplier(null);
    }

    public void responseBodyJson(final Object body) {
        try {
            val data = toJsonString(body);
            responseBody(data);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void headers(final Map<String, String> headers) {
        this.worker.getHeaders().putAll(headers);
    }

    public void responseBodySupplier(final Supplier<Resource> sup) {
        this.worker.setResource(null);
        this.worker.setResourceSupplier(sup);
    }

    public int getRequestCount() {
        return this.worker.getRequestCounter().get();
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
        try {
            this.worker.stop();
            this.workerThread.join();
            ALL_PORTS.remove(getPort());
        } catch (final InterruptedException e) {
            LoggingUtils.error(LOGGER, e);
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
    private static final class Worker implements Runnable {

        /**
         * Separates HTTP header from body.
         */
        private static final String SEPARATOR = "\r\n";

        /**
         * Response buffer size.
         */
        private static final int BUFFER_SIZE = 2048;

        @Getter
        private final Map<String, String> headers = new HashMap<>();

        private final ServerSocket serverSocket;

        @Setter
        private String contentType;

        private final Function<Socket, Object> functionToExecute;

        @Setter
        private HttpStatus status;

        @Setter
        private Resource resource;

        @Setter
        private Supplier<Resource> resourceSupplier;

        @Getter
        private final AtomicInteger requestCounter = new AtomicInteger(0);

        private boolean running;

        Worker(final ServerSocket sock, final String contentType) {
            this(sock, null, contentType, HttpStatus.OK);
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

        Worker(final ServerSocket serverSocket,
               final ByteArrayResource resource,
               final HttpStatus status,
               final String contentType,
               final Map<String, String> headers) {
            this(serverSocket, resource, contentType, status);
            this.headers.putAll(headers);
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        private static byte[] header(final String name, final Object value) {
            return String.format("%s: %s%s", name, value, SEPARATOR).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public synchronized void run() {
            while (this.running) {
                try (val socket = serverSocket.accept()) {
                    val givenHeaders = new HashMap<String, String>();
                    val in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    var line = StringUtils.EMPTY;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        if (line.contains(":")) {
                            val name = line.substring(0, line.indexOf(':')).trim();
                            val value = line.substring(line.indexOf(':') + 1).trim();
                            givenHeaders.put(name, value);
                        }
                    }
                    LOGGER.debug("Headers are [{}]", givenHeaders);

                    requestCounter.incrementAndGet();

                    if (functionToExecute != null) {
                        LOGGER.trace("Executed function with result [{}]", functionToExecute.apply(socket));
                    } else {
                        writeResponse(socket);
                    }
                    socket.shutdownOutput();
                    Thread.sleep(200);
                } catch (final SocketException e) {
                    LOGGER.debug("Stopping on socket close: [{}]", e.getMessage(), e);
                    this.running = false;
                } catch (final Exception e) {
                    LoggingUtils.error(LOGGER, e);
                }
            }
        }

        public void stop() {
            try {
                ALL_PORTS.remove(getPort());
                serverSocket.close();
            } catch (final IOException e) {
                LOGGER.trace("Exception when closing the server socket: [{}]", e.getMessage());
            }
        }

        private void writeResponse(final Socket socket) throws IOException {
            if (resource == null) {
                this.resource = resourceSupplier.get();
            }

            if (resource != null) {
                LOGGER.debug("Socket response for resource [{}]", resource.getDescription());
                val out = socket.getOutputStream();

                val statusLine = String.format("HTTP/1.1 %s %s%s", status.value(), status.getReasonPhrase(), SEPARATOR);
                out.write(statusLine.getBytes(StandardCharsets.UTF_8));
                out.write(header("Content-Length", this.resource.contentLength()));
                out.write(header("Content-Type", this.contentType));
                headers.forEach(Unchecked.biConsumer((key, value) -> out.write(header(key, value))));
                out.write(SEPARATOR.getBytes(StandardCharsets.UTF_8));

                val buffer = new byte[BUFFER_SIZE];
                try {
                    try (val in = resource.getInputStream()) {
                        var count = 0;
                        while ((count = in.read(buffer)) > -1) {
                            out.write(buffer, 0, count);
                        }
                    }
                } catch (final SocketException e) {
                    LOGGER.debug("Error while writing response, current response buffer [{}], response length [{}]",
                        buffer, this.resource.contentLength());
                    throw e;
                }
                out.flush();
                LOGGER.debug("Wrote response for resource [{}] for [{}]", resource.getDescription(), resource.contentLength());
            }
        }
    }

    private static int getRandomPort() {
        var port = 0;
        while (port == 0 || ALL_PORTS.contains(port)) {
            port = RandomUtils.nextInt(STARTING_PORT_RANGE, ENDING_PORT_RANGE);
        }
        ALL_PORTS.add(port);
        return port;
    }

    private static String toJsonString(final Object body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot convert object to JSON ", e);
        }
    }
}
