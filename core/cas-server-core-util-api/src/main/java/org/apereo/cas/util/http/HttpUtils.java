package org.apereo.cas.util.http;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.InMemoryDnsResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.jspecify.annotations.Nullable;
import org.springframework.core.retry.Retryable;
import org.springframework.http.MediaType;

/**
 * This is {@link HttpUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class HttpUtils {
    /**
     * A shared client is keyed by the few inputs that decide how it is built, and in a deployment
     * those take one or two distinct values. The bound exists only so that an unexpected source of
     * fresh identities cannot grow the registry without limit; past it, callers fall back to a
     * client of their own that is closed when the call completes.
     */
    private static final int MAXIMUM_SHARED_CLIENTS = 16;

    /**
     * Pool limits for a shared client. A per-call client had a pool to itself and so no effective
     * ceiling; a shared pool does, and the library's defaults of 25 total and 5 per route would
     * turn into one.
     */
    private static final int MAXIMUM_TOTAL_CONNECTIONS = 200;

    private static final int MAXIMUM_CONNECTIONS_PER_ROUTE = 50;

    private static final Object DEFAULT_CLIENT_IDENTITY = new Object();

    private static final Map<SharedClientKey, CloseableHttpClient> SHARED_CLIENTS = new ConcurrentHashMap<>();

    private static final Timeout CONNECT_TIMEOUT_IN_MILLISECONDS = getTimeout("connectionTimeout");
    private static final Timeout SOCKET_TIMEOUT_IN_MILLISECONDS = getTimeout("socketTimeout");
    private static final Timeout CONNECT_TTL_TIMEOUT_IN_MILLISECONDS = getTimeout("connectionTimeToLive");
    private static final Timeout CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS = getTimeout("connectionRequest");

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(HttpUtils::closeSharedClients, "cas-http-shared-clients"));
    }

    private static Timeout getTimeout(final String setting) {
        val timeoutValue = StringUtils.defaultIfBlank(System.getProperty(HttpUtils.class.getName() + '.' + setting), "5000");
        return Timeout.ofMilliseconds(Long.parseLong(timeoutValue));
    }

    /**
     * Execute http request and produce a response.
     *
     * @param execution the request
     * @return the http response
     */
    public HttpResponse execute(final HttpExecutionRequest execution) {
        val uri = buildHttpUri(execution.getUrl().trim(), execution.getParameters());
        val request = getHttpRequestByMethod(execution.getMethod().name().toLowerCase(Locale.ENGLISH).trim(), execution.getEntity(), uri);
        CloseableHttpClient bespokeClient = null;
        try {
            request.setHeaders(execution.getHeaders().entrySet().stream()
                .map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .toArray(BasicHeader[]::new));
            prepareHttpRequest(request, execution);
            val resolvedClient = resolveHttpClient(execution);
            bespokeClient = resolvedClient.shared() ? null : resolvedClient.httpClient();
            val client = resolvedClient.httpClient();
            return FunctionUtils.doAndRetry((Retryable<HttpResponse>) () -> {
                LOGGER.trace("Sending HTTP request to [{}]", request.getUri());
                val res = client.execute(request, HttpRequestUtils.HTTP_CLIENT_RESPONSE_HANDLER);
                if (res == null || org.springframework.http.HttpStatus.valueOf(res.getCode()).is5xxServerError()) {
                    throw new HttpRequestExecutionException(res);
                }
                return res;
            }, execution.getMaximumRetryAttempts());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            if (e.getCause() instanceof final SSLHandshakeException she) {
                val sanitizedUrl = FunctionUtils.doUnchecked(
                    () -> new URIBuilder(execution.getUrl()).removeQuery().clearParameters().build().toASCIIString());
                LoggingUtils.error(LOGGER, "SSL error accessing: [" + sanitizedUrl + ']', she);
                return new BasicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, sanitizedUrl);
            }
            if (e.getCause() instanceof final HttpRequestExecutionException hre && hre.getResponse() != null) {
                val response = new BasicClassicHttpResponse(hre.getResponse().getCode(), hre.getResponse().getReasonPhrase());
                response.setHeaders(hre.getResponse().getHeaders());
                if (hre.getResponse() instanceof final HttpEntityContainer entityContainer) {
                    response.setEntity(entityContainer.getEntity());
                }
                return response;
            }
        } finally {
            closeQuietly(bespokeClient);
        }
        return new BasicHttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE);
    }

    /**
     * Resolves the client to run a request with, reusing one wherever the request does not need a
     * client of its own.
     * <p>
     * A client is expensive: it owns a pooling connection manager, and every connection that pool
     * opens pays a TCP and, over TLS, a handshake. Building one per call threw all of that away
     * after a single request and left the pool — and the idle socket in it — for nobody to close,
     * since nothing would ever lease from it again. Clients are therefore cached by exactly what
     * decides how they are built: the TLS material, whether redirects are followed, and whether
     * automatic retries are on. Timeouts come from system properties read once, so they are the
     * same for every client and do not belong in the key.
     * <p>
     * A proxy or a DNS override is caller-supplied and unbounded, so those requests get a client of
     * their own, which the caller closes when the request completes.
     *
     * @param execution the execution request
     * @return the client, and whether it is shared and so must not be closed by the caller
     * @throws Exception the exception
     */
    private static ResolvedHttpClient resolveHttpClient(final HttpExecutionRequest execution) throws Exception {
        if (StringUtils.isNotBlank(execution.getProxyUrl()) || !execution.getResolvedAddresses().isEmpty()) {
            return new ResolvedHttpClient(getHttpClient(execution, false), false);
        }
        val clientIdentity = Optional.ofNullable(execution.getHttpClient())
            .map(HttpClient::httpClientFactory)
            .map(Object.class::cast)
            .orElse(DEFAULT_CLIENT_IDENTITY);
        val key = new SharedClientKey(clientIdentity,
            execution.isRedirectsEnabled(), execution.getMaximumRetryAttempts() > 1);
        val shared = SHARED_CLIENTS.get(key);
        if (shared != null) {
            return new ResolvedHttpClient(shared, true);
        }
        if (SHARED_CLIENTS.size() >= MAXIMUM_SHARED_CLIENTS) {
            LOGGER.warn("Reached [{}] shared HTTP clients; building a dedicated client for this request instead. "
                + "This is unexpected and suggests HTTP client factories are being created per request.", MAXIMUM_SHARED_CLIENTS);
            return new ResolvedHttpClient(getHttpClient(execution, false), false);
        }
        return new ResolvedHttpClient(SHARED_CLIENTS.computeIfAbsent(key,
            _ -> FunctionUtils.doUnchecked(() -> getHttpClient(execution, true))), true);
    }

    private static void closeSharedClients() {
        SHARED_CLIENTS.values().forEach(HttpUtils::closeQuietly);
        SHARED_CLIENTS.clear();
    }

    private static void closeQuietly(final @Nullable CloseableHttpClient client) {
        if (client != null) {
            FunctionUtils.doAndHandle(_ -> client.close());
        }
    }

    /**
     * Identifies a client that may be shared between requests. Two requests may use one client when
     * every input to its construction matches.
     *
     * @param clientIdentity   the HTTP client factory supplying TLS material, or a sentinel when none
     * @param redirectsEnabled whether redirects are followed
     * @param automaticRetries whether the library's own retry handler is left in place
     */
    private record SharedClientKey(Object clientIdentity, boolean redirectsEnabled, boolean automaticRetries) {
    }

    /**
     * A client, and whether it belongs to the shared registry. A client that does not must be closed
     * once the request it was built for completes.
     *
     * @param httpClient the client
     * @param shared     whether the client is shared and must be left open
     */
    private record ResolvedHttpClient(CloseableHttpClient httpClient, boolean shared) {
    }

    /**
     * Create http client.
     *
     * @param execution http execution request
     * @return http execution request
     */
    private static CloseableHttpClient getHttpClient(final HttpExecutionRequest execution,
                                                     final boolean shared) throws Exception {
        val builder = getHttpClientBuilder(execution, shared);
        if (StringUtils.isNotBlank(execution.getProxyUrl())) {
            val proxyEndpoint = new URI(execution.getProxyUrl()).toURL();
            val proxy = new HttpHost(proxyEndpoint.getHost(), proxyEndpoint.getPort());
            builder.setProxy(proxy);
        }
        return builder.build();
    }

    /**
     * Close the response.
     *
     * @param response the response to close
     */
    public void close(final @Nullable HttpResponse response) {
        if (response instanceof final CloseableHttpResponse closeableHttpResponse) {
            FunctionUtils.doAndHandle(_ -> closeableHttpResponse.close());
        }
    }

    /**
     * Create basic auth headers.
     *
     * @param basicAuthUser     the basic auth user
     * @param basicAuthPassword the basic auth password
     * @return http headers
     */
    public org.springframework.http.HttpHeaders createBasicAuthHeaders(final String basicAuthUser,
                                                                       final String basicAuthPassword) {
        return HttpUtils.createBasicAuthHeaders(basicAuthUser, basicAuthPassword, "US-ASCII");
    }

    /**
     * Create basic auth headers.
     *
     * @param basicAuthUser     the basic auth user
     * @param basicAuthPassword the basic auth password
     * @param basicCharset      The charset used to encode auth header
     * @return the org . springframework . http . http headers
     */
    public org.springframework.http.HttpHeaders createBasicAuthHeaders(final String basicAuthUser,
                                                                       final String basicAuthPassword,
                                                                       final String basicCharset) {
        val acceptHeaders = new org.springframework.http.HttpHeaders();
        acceptHeaders.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        if (StringUtils.isNotBlank(basicAuthUser) && StringUtils.isNotBlank(basicAuthPassword)) {
            val authorization = basicAuthUser + ':' + basicAuthPassword;
            val basic = EncodingUtils.encodeBase64(authorization.getBytes(Charset.forName(basicCharset)));
            acceptHeaders.set(org.springframework.http.HttpHeaders.AUTHORIZATION, "Basic " + basic);
        }
        return acceptHeaders;
    }

    /**
     * Create http request.
     *
     * @param method http request method
     * @param entity http request body
     * @param uri    http request uri
     * @return http request with get method
     */
    private HttpUriRequest getHttpRequestByMethod(final String method, final String entity, final URI uri) {
        if ("post".equalsIgnoreCase(method)) {
            val request = new HttpPost(uri);
            if (StringUtils.isNotBlank(entity)) {
                val stringEntity = new StringEntity(entity, StandardCharsets.UTF_8);
                request.setEntity(stringEntity);
            }
            return request;
        } else if ("patch".equalsIgnoreCase(method)) {
            val request = new HttpPatch(uri);
            if (StringUtils.isNotBlank(entity)) {
                val stringEntity = new StringEntity(entity, StandardCharsets.UTF_8);
                request.setEntity(stringEntity);
            }
            return request;
        } else if ("delete".equalsIgnoreCase(method)) {
            return new HttpDelete(uri);
        }

        return new HttpGet(uri);
    }

    /**
     * Prepare http request. Tries to set the authorization header
     * in cases where the URL endpoint does not actually produce the header
     * on its own.
     *
     * @param request   the request
     * @param execution the execution request
     */
    private void prepareHttpRequest(final HttpUriRequest request,
                                    final HttpExecutionRequest execution) {
        if (execution.isBasicAuthentication()) {
            val auth = EncodingUtils.encodeBase64(execution.getBasicAuthUsername() + ':' + execution.getBasicAuthPassword());
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        }
        if (execution.isBearerAuthentication()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + execution.getBearerToken());
        }
    }

    /**
     * Build URI for http request.
     *
     * @param url        used for URI
     * @param parameters for URI
     * @return URI for http request
     */
    private URI buildHttpUri(final String url, final Map<String, String> parameters) {
        return FunctionUtils.doUnchecked(() -> {
            val uriBuilder = new URIBuilder(url);
            parameters.forEach(uriBuilder::addParameter);
            return uriBuilder.build();
        });
    }

    private HttpClientBuilder getHttpClientBuilder(final HttpExecutionRequest execution, final boolean shared) {
        val requestConfig = RequestConfig.custom();
        requestConfig.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
        requestConfig.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS);
        requestConfig.setRedirectsEnabled(execution.isRedirectsEnabled());

        val builder = HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(requestConfig.build());
        if (execution.getMaximumRetryAttempts() <= 1) {
            builder.disableAutomaticRetries();
        }
        val socketFactory = Optional.ofNullable(execution.getHttpClient())
            .map(HttpClient::httpClientFactory)
            .filter(factory -> Objects.nonNull(factory.getSslSocketFactory()))
            .map(HttpClientFactory::getSslSocketFactory)
            .orElseGet(() -> getSslConnectionSocketFactory(execution));

        val connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(socketFactory)
            .setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(SOCKET_TIMEOUT_IN_MILLISECONDS)
                .build())
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
            .setConnPoolPolicy(PoolReusePolicy.LIFO)
            .setDefaultConnectionConfig(buildConnectionConfig(shared));
        if (!execution.getResolvedAddresses().isEmpty()) {
            val dnsResolver = new InMemoryDnsResolver();
            execution.getResolvedAddresses().forEach(dnsResolver::add);
            connectionManagerBuilder.setDnsResolver(dnsResolver);
        }
        val connectionManager = connectionManagerBuilder.build();
        if (shared) {
            connectionManager.setMaxTotal(MAXIMUM_TOTAL_CONNECTIONS);
            connectionManager.setDefaultMaxPerRoute(MAXIMUM_CONNECTIONS_PER_ROUTE);
        }
        builder.setConnectionManager(connectionManager);
        return builder;
    }

    /**
     * Builds the connection configuration.
     * <p>
     * A shared client keeps its connections between requests, which is the point of sharing them,
     * and that makes it possible to lease one whose peer has gone away since it was last used --
     * a restarted service, a load balancer that dropped it. Such a connection is revalidated before
     * every lease. A client built for a single request cannot have that problem and is not charged
     * for the check.
     *
     * @param shared whether the configuration is for a shared client
     * @return the connection configuration
     */
    private static ConnectionConfig buildConnectionConfig(final boolean shared) {
        val config = ConnectionConfig.custom()
            .setTimeToLive(CONNECT_TTL_TIMEOUT_IN_MILLISECONDS)
            .setSocketTimeout(SOCKET_TIMEOUT_IN_MILLISECONDS)
            .setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
        if (shared) {
            config.setValidateAfterInactivity(Timeout.ofMilliseconds(0));
        }
        return config.build();
    }

    private static SSLConnectionSocketFactory getSslConnectionSocketFactory(final HttpExecutionRequest execution) {
        val builder = SSLConnectionSocketFactoryBuilder.create().useSystemProperties();
        Optional.ofNullable(execution.getHttpClient())
            .map(HttpClient::httpClientFactory)
            .ifPresentOrElse(factory -> {
                builder.setHostnameVerifier(factory.getHostnameVerifier());
                builder.setSslContext(factory.getSslContext());
            }, () -> builder.setSslContext(SSLContexts.createDefault()).setHostnameVerifier(new DefaultHostnameVerifier()));
        return builder.build();
    }

    @RequiredArgsConstructor
    @Getter
    private static final class HttpRequestExecutionException extends IllegalStateException {
        @Serial
        private static final long serialVersionUID = 3764678971716744728L;

        private final HttpResponse response;
    }
}
