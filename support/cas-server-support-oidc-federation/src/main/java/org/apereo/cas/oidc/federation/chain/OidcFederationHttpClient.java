package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.dynareg.DefaultOidcClientIdMetadataDocumentFetcher;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;

/**
 * Sends the requests of the federation: entity configurations, subordinate statements, historical keys and trust mark
 * status requests. Every request is bounded in time and size: the whole exchange, from resolving the host name to the
 * last byte of the response, through the TLS handshake and the response headers, must complete before the deadline of
 * the running resolution, or within the resolution timeout outside a resolution, however slowly the other side answers;
 * the exchange runs on a thread of its own, which is abandoned, and its connection closed, once the deadline passes. At
 * most {@value #MAX_EXCHANGES} exchanges run at once, abandoned ones included, so that hosts that never answer cannot pile
 * them up. Its response may not exceed a fixed size. Requests are only sent over {@code https}, as federation entity
 * identifiers and endpoints are (OpenID Federation 1.0, sections 1.2 and 5.1.1), and never to a link-local, unspecified
 * or multicast address, such as the metadata service of a cloud provider, including one embedded in an IPv6 address.
 * Addresses written in the URL are always checked, and host names when they are resolved here, for a direct connection
 * or one through a SOCKS proxy of the JVM; a host name sent through an HTTP proxy of the JVM is resolved by the proxy,
 * which then decides where it may go. Redirects are not followed, and TLS is configured by CAS rather than by defaults
 * shared with the rest of the JVM.
 * <p>
 * A request that runs out of time while it is sent, having been given the time a request normally has, tells that its
 * host does not answer, and so does one whose host does not accept the connection in the time a connection is given,
 * which a connection is always given; one that cannot be given the time of a request, because the resolution has used
 * it up, tells nothing about
 * its host and fails with {@link OutOfTimeException}, and one that finds no room among the running requests fails with
 * {@link TooManyRequestsException}; both leave the running resolution unable to tell, so it is unavailable rather than
 * failed.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OidcFederationHttpClient {
    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "oidcFederationHttpClient";

    /**
     * The largest response accepted, in bytes.
     */
    public static final int MAX_RESPONSE_BYTES = 256 * 1024;

    private static final int CONNECT_TIMEOUT_MILLIS = 2_000;

    private static final int READ_TIMEOUT_MILLIS = 5_000;

    private static final int BUFFER_SIZE = 8_192;

    private static final int MAX_EXCHANGES = 64;

    private static final ExecutorService EXCHANGES = Executors.newVirtualThreadPerTaskExecutor();

    private static final Semaphore RUNNING_EXCHANGES = new Semaphore(MAX_EXCHANGES);

    private final Duration resolutionTimeout;

    private final CasSSLContext sslContext;

    public OidcFederationHttpClient(final OidcFederationProperties properties) {
        this(properties, CasSSLContext.system());
    }

    public OidcFederationHttpClient(final OidcFederationProperties properties, final CasSSLContext sslContext) {
        this.resolutionTimeout = Beans.newDuration(properties.getResolutionTimeout());
        this.sslContext = sslContext;
    }

    /**
     * Send the request.
     *
     * @param request the request
     * @return the response
     * @throws IOException when the request cannot be sent, is not allowed, takes too long, or its response is too large
     */
    public HTTPResponse send(final HTTPRequest request) throws IOException {
        val deadline = OidcFederationResolutionLimiter.currentBudget()
            .map(OidcFederationResolutionLimiter.RequestBudget::getDeadline)
            .orElseGet(() -> Instant.now(Clock.systemUTC()).plus(resolutionTimeout));
        val remaining = remainingMillis(deadline);
        if (!RUNNING_EXCHANGES.tryAcquire()) {
            throw indeterminate(new TooManyRequestsException("Too many federation requests are running; %s is not requested".formatted(request.getURL())));
        }
        val connection = new AtomicReference<HttpURLConnection>();
        val claimed = new AtomicBoolean();
        val exchange = submit(request, deadline, connection, claimed);
        try {
            return exchange.get(remaining, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            abandon(exchange, connection, claimed);
            throw remaining >= READ_TIMEOUT_MILLIS
                ? new SocketTimeoutException("Federation request to %s did not complete in time".formatted(request.getURL()))
                : indeterminate(new OutOfTimeException());
        } catch (final InterruptedException e) {
            abandon(exchange, connection, claimed);
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Federation request to %s is interrupted".formatted(request.getURL()));
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof final RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (e.getCause() instanceof OutOfTimeException) {
                throw remaining >= READ_TIMEOUT_MILLIS
                    ? new SocketTimeoutException("Federation request to %s did not complete in time".formatted(request.getURL()))
                    : indeterminate(new OutOfTimeException());
            }
            throw e.getCause() instanceof final IOException ioException ? ioException : new IOException(e.getCause());
        }
    }

    /**
     * Run the exchange on a thread of its own. Whichever comes first, the exchange starting or it being abandoned,
     * claims it and gives back its place among the running exchanges when it is done, so that an exchange abandoned
     * before it starts never runs and its place is not lost.
     *
     * @param request    the request
     * @param deadline   the deadline
     * @param connection the connection of the exchange, once opened
     * @param claimed    whether the exchange was started or abandoned
     * @return the exchange
     */
    private Future<HTTPResponse> submit(final HTTPRequest request, final Instant deadline,
                                        final AtomicReference<HttpURLConnection> connection, final AtomicBoolean claimed) {
        try {
            return EXCHANGES.submit(() -> {
                if (!claimed.compareAndSet(false, true)) {
                    throw new OutOfTimeException();
                }
                try {
                    return exchange(request, deadline, connection);
                } finally {
                    RUNNING_EXCHANGES.release();
                }
            });
        } catch (final RejectedExecutionException e) {
            RUNNING_EXCHANGES.release();
            throw e;
        }
    }

    private HTTPResponse exchange(final HTTPRequest request, final Instant deadline,
                                  final AtomicReference<HttpURLConnection> connectionHolder)
            throws IOException {
        val url = request.getURL();
        verifyTarget(url);
        val connection = (HttpURLConnection) url.openConnection();
        connectionHolder.set(connection);
        try {
            if (connection instanceof final HttpsURLConnection httpsConnection) {
                httpsConnection.setSSLSocketFactory(sslContext.getSslContext().getSocketFactory());
                httpsConnection.setHostnameVerifier(sslContext.getHostnameVerifier());
            }
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setRequestMethod(request.getMethod().name());
            request.getHeaderMap().forEach((name, values) -> values.forEach(value -> connection.addRequestProperty(name, value)));
            val body = request.getBody();
            if (body != null) {
                connection.setDoOutput(true);
                try (val output = connection.getOutputStream()) {
                    output.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            val status = connection.getResponseCode();
            val response = new HTTPResponse(status);
            val contentType = connection.getContentType();
            if (StringUtils.isNotBlank(contentType)) {
                response.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
            }
            try (val input = status >= HttpURLConnection.HTTP_BAD_REQUEST ? connection.getErrorStream() : connection.getInputStream()) {
                if (input != null) {
                    response.setBody(read(input, deadline));
                }
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Refuse a request that is not sent over {@code https}, or whose host resolves to a link-local, unspecified or
     * multicast address, including one embedded in an IPv4-compatible or NAT64 IPv6 address. Addresses of private and
     * loopback networks are allowed, since federations may run there. A request sent through an HTTP proxy is not
     * resolved here, since the proxy resolves it.
     *
     * @param url the URL of the request
     * @throws IOException when the request is not allowed
     */
    private static void verifyTarget(final URL url) throws IOException {
        if (!Strings.CI.equals(url.getProtocol(), "https")) {
            throw new RefusedRequestException("Federation request to %s is not sent over https".formatted(url));
        }
        if (!DefaultOidcClientIdMetadataDocumentFetcher.isAllowedWithPrivateNetworks(url)) {
            throw new RefusedRequestException("Federation request to %s resolves to an address that is not allowed".formatted(url));
        }
    }

    private static void abandon(final Future<HTTPResponse> exchange, final AtomicReference<HttpURLConnection> connection,
                                final AtomicBoolean claimed) {
        exchange.cancel(true);
        Optional.ofNullable(connection.get()).ifPresent(HttpURLConnection::disconnect);
        if (claimed.compareAndSet(false, true)) {
            RUNNING_EXCHANGES.release();
        }
    }

    private static long remainingMillis(final Instant deadline) throws OutOfTimeException {
        val remaining = Duration.between(Instant.now(Clock.systemUTC()), deadline).toMillis();
        if (remaining <= 0) {
            throw indeterminate(new OutOfTimeException());
        }
        return remaining;
    }

    private static <T extends IOException> T indeterminate(final T failure) {
        OidcFederationResolutionLimiter.currentBudget().ifPresent(OidcFederationResolutionLimiter.RequestBudget::markIndeterminate);
        return failure;
    }

    private static String read(final InputStream input, final Instant deadline) throws IOException {
        val output = new ByteArrayOutputStream();
        val buffer = new byte[BUFFER_SIZE];
        var read = input.read(buffer);
        while (read != -1) {
            output.write(buffer, 0, read);
            if (output.size() > MAX_RESPONSE_BYTES) {
                throw new IOException("Federation response is larger than %s bytes".formatted(MAX_RESPONSE_BYTES));
            }
            if (!Instant.now(Clock.systemUTC()).isBefore(deadline)) {
                throw new OutOfTimeException();
            }
            read = input.read(buffer);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    /**
     * A request that is not sent because too many requests are running, abandoned ones included; it tells nothing about
     * its host.
     */
    public static final class TooManyRequestsException extends IOException {
        @Serial
        private static final long serialVersionUID = 5137402962274021863L;

        TooManyRequestsException(final String message) {
            super(message);
        }
    }

    /**
     * A request that is not allowed: not sent over {@code https}, or to an address that is not allowed.
     */
    public static final class RefusedRequestException extends IOException {
        @Serial
        private static final long serialVersionUID = -2795187338405473113L;

        RefusedRequestException(final String message) {
            super(message);
        }
    }

    /**
     * A request that could not be given the time a request normally has, because the resolution it belongs to has used
     * it up; it tells nothing about whether its host answers.
     */
    public static final class OutOfTimeException extends SocketTimeoutException {
        @Serial
        private static final long serialVersionUID = 4218530781651230571L;

        OutOfTimeException() {
            super("Federation resolution has run out of time");
        }
    }
}
