package org.apereo.cas.util.http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.classic.ConnectionBackoffStrategy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.DefaultBackoffStrategy;
import org.apache.hc.client5.http.impl.classic.FutureRequestExecutionService;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The factory to build a {@link SimpleHttpClient}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
@Setter
@Getter
@Slf4j
public class SimpleHttpClientFactoryBean implements HttpClientFactory {

    /**
     * Max connections per route.
     */
    public static final int MAX_CONNECTIONS_PER_ROUTE = 50;

    private static final int MAX_POOLED_CONNECTIONS = 100;

    private static final int DEFAULT_TIMEOUT = 5000;

    private static final int TERMINATION_TIMEOUT_SECONDS = 5;

    /**
     * The default status codes we accept.
     */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = {HttpURLConnection.HTTP_OK,
        HttpURLConnection.HTTP_NOT_MODIFIED, HttpURLConnection.HTTP_MOVED_TEMP,
        HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_ACCEPTED,
        HttpURLConnection.HTTP_NO_CONTENT};

    /**
     * The Max pooled connections.
     */
    private int maxPooledConnections = MAX_POOLED_CONNECTIONS;

    /**
     * The Max connections per each route connections.
     */
    private int maxConnectionsPerRoute = MAX_CONNECTIONS_PER_ROUTE;

    /**
     * List of HTTP status codes considered valid by the caller.
     */
    private List<Integer> acceptableCodes = IntStream.of(DEFAULT_ACCEPTABLE_CODES).boxed().toList();

    private long connectionTimeout = DEFAULT_TIMEOUT;

    private long socketTimeout = DEFAULT_TIMEOUT;

    private long responseTimeout = DEFAULT_TIMEOUT;

    /**
     * The redirection strategy by default, using http status codes.
     */
    private RedirectStrategy redirectionStrategy = new DefaultRedirectStrategy();

    /**
     * The socket factory to be used when verifying the validity of the endpoint.
     */
    private LayeredConnectionSocketFactory sslSocketFactory;

    /**
     * The hostname verifier to be used when verifying the validity of the endpoint.
     */
    private HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();

    /**
     * The CAS SSL context used to create ssl socket factories, etc.
     */
    private SSLContext sslContext;

    /**
     * X509 trust managers.
     */
    private TrustManager[] trustManagers;

    /**
     * The credentials provider for endpoints that require authentication.
     */
    private CredentialsProvider credentialsProvider;

    /**
     * The cookie store for authentication.
     */
    private CookieStore cookieStore;

    /**
     * Interface for deciding whether a connection can be reused for subsequent requests and should be kept alive.
     **/
    private ConnectionReuseStrategy connectionReuseStrategy = new DefaultConnectionReuseStrategy();

    /**
     * Interface for deciding how long a connection can remain idle before being reused.
     */
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new DefaultConnectionKeepAliveStrategy();

    /**
     * When managing a dynamic number of connections for a given route, this strategy assesses whether a
     * given request execution outcome should result in a backoff
     * signal or not, based on either examining the Throwable that resulted or by examining
     * the resulting response (e.g. for its status code).
     */
    private ConnectionBackoffStrategy connectionBackoffStrategy = new DefaultBackoffStrategy();

    /**
     * Strategy interface that allows API users to plug in their own logic to control whether or not a retry
     * should automatically be done, how many times it should be retried and so on.
     */
    private HttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy();

    /**
     * Default headers to be sent.
     **/
    private Collection<? extends Header> defaultHeaders = new ArrayList<>();

    /**
     * Default strategy implementation for proxy host authentication.
     **/
    private AuthenticationStrategy proxyAuthenticationStrategy = new DefaultAuthenticationStrategy();

    /**
     * Determines whether circular redirects (redirects to the same location) should be allowed.
     **/
    private boolean circularRedirectsAllowed = true;

    /**
     * Determines whether authentication should be handled automatically.
     **/
    private boolean authenticationEnabled;

    /**
     * Determines whether redirects should be handled automatically.
     **/
    private boolean redirectsEnabled = true;

    /**
     * The executor service used to create a {@link #buildRequestExecutorService}.
     */
    private ExecutorService executorService;

    private HttpHost proxy;

    @Override
    public SimpleHttpClient getObject() {
        val httpClient = buildHttpClient();
        val requestExecutorService = buildRequestExecutorService(httpClient);
        val codes = this.acceptableCodes.stream().sorted().collect(Collectors.toList());
        return new SimpleHttpClient(codes, httpClient, requestExecutorService, this);
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleHttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void destroy() {
        if (this.executorService != null) {
            try {
                this.executorService.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (final Exception e) {
                LOGGER.trace(e.getMessage(), e);
            }
            this.executorService = null;
        }
    }

    /**
     * Build a HTTP client based on the current properties.
     *
     * @return the built HTTP client
     */
    @SuppressWarnings("java:S2095")
    private CloseableHttpClient buildHttpClient() {
        val sslFactory = Optional.ofNullable(this.sslSocketFactory)
            .orElseGet(() -> new SSLConnectionSocketFactory(
                ObjectUtils.defaultIfNull(this.sslContext, SSLContexts.createDefault()),
                ObjectUtils.defaultIfNull(this.hostnameVerifier, new DefaultHostnameVerifier())));
        
        val plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        val registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", plainSocketFactory)
            .register("https", sslFactory)
            .build();
        
        val connConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(this.connectionTimeout))
            .setSocketTimeout(Timeout.ofMilliseconds(this.socketTimeout))
            .setValidateAfterInactivity(Timeout.ofMilliseconds(DEFAULT_TIMEOUT))
            .build();

        val connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(this.maxPooledConnections);
        connectionManager.setDefaultMaxPerRoute(this.maxConnectionsPerRoute);
        connectionManager.setValidateAfterInactivity(Timeout.ofMilliseconds(DEFAULT_TIMEOUT));
        connectionManager.setDefaultConnectionConfig(connConfig);

        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(this.connectionTimeout))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(this.connectionTimeout))
            .setCircularRedirectsAllowed(this.circularRedirectsAllowed)
            .setRedirectsEnabled(this.redirectsEnabled)
            .setAuthenticationEnabled(this.authenticationEnabled)
            .setResponseTimeout(Timeout.ofMilliseconds(this.responseTimeout))
            .build();

        val builder = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .setRedirectStrategy(this.redirectionStrategy)
            .setDefaultCredentialsProvider(this.credentialsProvider)
            .setDefaultCookieStore(this.cookieStore)
            .setConnectionReuseStrategy(this.connectionReuseStrategy)
            .setKeepAliveStrategy(this.connectionKeepAliveStrategy)
            .setConnectionBackoffStrategy(this.connectionBackoffStrategy)
            .setRetryStrategy(this.retryStrategy)
            .setProxyAuthenticationStrategy(this.proxyAuthenticationStrategy)
            .setDefaultHeaders(this.defaultHeaders)
            .setProxy(this.proxy)
            .useSystemProperties();
        return builder.build();
    }
    
    private FutureRequestExecutionService buildRequestExecutorService(final CloseableHttpClient httpClient) {
        if (this.executorService == null) {
            this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        }
        return new FutureRequestExecutionService(httpClient, this.executorService);
    }

    /**
     * The default http client.
     */
    public static class DefaultHttpClient extends SimpleHttpClientFactoryBean {
    }
}
