package org.apereo.cas.util.http;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.ConnectionBackoffStrategy;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultBackoffStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.PreDestroy;
import javax.net.ssl.HostnameVerifier;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The factory to build a {@link SimpleHttpClient}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
public class SimpleHttpClientFactoryBean implements FactoryBean<SimpleHttpClient> {

    /**
     * Max connections per route.
     */
    public static final int MAX_CONNECTIONS_PER_ROUTE = 50;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClientFactoryBean.class);

    private static final int MAX_POOLED_CONNECTIONS = 100;

    private static final int DEFAULT_THREADS_NUMBER = 200;

    private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * The default status codes we accept.
     */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[]{
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED, HttpURLConnection.HTTP_NO_CONTENT};

    /**
     * 20% of the total of threads in the pool to handle overhead.
     */
    private static final int DEFAULT_QUEUE_SIZE = (int) (DEFAULT_THREADS_NUMBER * 0.2);

    /**
     * The number of threads used to build the pool of threads (if no executorService provided).
     */
    private int threadsNumber = DEFAULT_THREADS_NUMBER;

    /**
     * The queue size to absorb additional tasks when the threads pool is saturated (if no executorService provided).
     */
    private int queueSize = DEFAULT_QUEUE_SIZE;

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
    private List<Integer> acceptableCodes = IntStream.of(DEFAULT_ACCEPTABLE_CODES).boxed().collect(Collectors.toList());

    private long connectionTimeout = DEFAULT_TIMEOUT;

    private int readTimeout = DEFAULT_TIMEOUT;

    private RedirectStrategy redirectionStrategy = new DefaultRedirectStrategy();

    /**
     * The socket factory to be used when verifying the validity of the endpoint.
     */
    private SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();

    /**
     * The hostname verifier to be used when verifying the validity of the endpoint.
     */
    private HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();

    /**
     * The credentials provider for endpoints that require authentication.
     */
    private CredentialsProvider credentialsProvider;

    /**
     * The cookie store for authentication.
     */
    private CookieStore cookieStore;

    /**
     * Interface for deciding whether a connection can be re-used for subsequent requests and should be kept alive.
     **/
    private ConnectionReuseStrategy connectionReuseStrategy = new DefaultConnectionReuseStrategy();

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
    private ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = new DefaultServiceUnavailableRetryStrategy();

    /**
     * Default headers to be sent.
     **/
    private Collection<? extends Header> defaultHeaders = new ArrayList<>(0);

    /**
     * Default strategy implementation for proxy host authentication.
     **/
    private AuthenticationStrategy proxyAuthenticationStrategy = new ProxyAuthenticationStrategy();

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

    @Override
    public SimpleHttpClient getObject() {

        final CloseableHttpClient httpClient = buildHttpClient();

        final FutureRequestExecutionService requestExecutorService = buildRequestExecutorService(httpClient);

        return new SimpleHttpClient(this.acceptableCodes, httpClient, requestExecutorService);
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleHttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    /**
     * Build a HTTP client based on the current properties.
     *
     * @return the built HTTP client
     */
    private CloseableHttpClient buildHttpClient() {
        try {

            final ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
            final LayeredConnectionSocketFactory sslsf = this.sslSocketFactory;

            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", plainsf)
                    .register("https", sslsf)
                    .build();

            final PoolingHttpClientConnectionManager connMgmr = new PoolingHttpClientConnectionManager(registry);
            connMgmr.setMaxTotal(this.maxPooledConnections);
            connMgmr.setDefaultMaxPerRoute(this.maxConnectionsPerRoute);
            connMgmr.setValidateAfterInactivity(DEFAULT_TIMEOUT);

            final HttpHost httpHost = new HttpHost(InetAddress.getLocalHost());
            final HttpRoute httpRoute = new HttpRoute(httpHost);
            connMgmr.setMaxPerRoute(httpRoute, MAX_CONNECTIONS_PER_ROUTE);

            final RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(this.readTimeout)
                    .setConnectTimeout((int) this.connectionTimeout)
                    .setConnectionRequestTimeout((int) this.connectionTimeout)
                    .setCircularRedirectsAllowed(this.circularRedirectsAllowed)
                    .setRedirectsEnabled(this.redirectsEnabled)
                    .setAuthenticationEnabled(this.authenticationEnabled)
                    .build();


            final HttpClientBuilder builder = HttpClients.custom()
                    .setConnectionManager(connMgmr)
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(sslsf)
                    .setSSLHostnameVerifier(this.hostnameVerifier)
                    .setRedirectStrategy(this.redirectionStrategy)
                    .setDefaultCredentialsProvider(this.credentialsProvider)
                    .setDefaultCookieStore(this.cookieStore)
                    .setConnectionReuseStrategy(this.connectionReuseStrategy)
                    .setConnectionBackoffStrategy(this.connectionBackoffStrategy)
                    .setServiceUnavailableRetryStrategy(this.serviceUnavailableRetryStrategy)
                    .setProxyAuthenticationStrategy(this.proxyAuthenticationStrategy)
                    .setDefaultHeaders(this.defaultHeaders)
                    .useSystemProperties();

            return builder.build();

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Build a {@link FutureRequestExecutionService} from the current properties and a HTTP client.
     *
     * @param httpClient the provided HTTP client
     * @return the built request executor service
     */
    private FutureRequestExecutionService buildRequestExecutorService(final CloseableHttpClient httpClient) {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(this.threadsNumber, this.threadsNumber,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(this.queueSize));
        }

        return new FutureRequestExecutionService(httpClient, this.executorService);
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    public int getThreadsNumber() {
        return this.threadsNumber;
    }

    public void setThreadsNumber(final int threadsNumber) {
        this.threadsNumber = threadsNumber;
    }

    public int getQueueSize() {
        return this.queueSize;
    }

    public void setQueueSize(final int queueSize) {
        this.queueSize = queueSize;
    }

    public int getMaxPooledConnections() {
        return this.maxPooledConnections;
    }

    public void setMaxPooledConnections(final int maxPooledConnections) {
        this.maxPooledConnections = maxPooledConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return this.maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(final int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public List<Integer> getAcceptableCodes() {
        return new ArrayList<>(this.acceptableCodes);
    }

    public void setAcceptableCodes(final int[] acceptableCodes) {
        this.acceptableCodes = IntStream.of(acceptableCodes).boxed().collect(Collectors.toList());
    }

    public long getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(final long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }


    public void setReadTimeout(
            final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public RedirectStrategy getRedirectionStrategy() {
        return this.redirectionStrategy;
    }

    public void setRedirectionStrategy(final RedirectStrategy redirectionStrategy) {
        this.redirectionStrategy = redirectionStrategy;
    }

    public SSLConnectionSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }

    public void setSslSocketFactory(final SSLConnectionSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public void setHostnameVerifier(final HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public CredentialsProvider getCredentialsProvider() {
        return this.credentialsProvider;
    }

    public void setCredentialsProvider(final CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public CookieStore getCookieStore() {
        return this.cookieStore;
    }

    public void setCookieStore(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public ConnectionReuseStrategy getConnectionReuseStrategy() {
        return this.connectionReuseStrategy;
    }

    public void setConnectionReuseStrategy(final ConnectionReuseStrategy connectionReuseStrategy) {
        this.connectionReuseStrategy = connectionReuseStrategy;
    }

    public ConnectionBackoffStrategy getConnectionBackoffStrategy() {
        return this.connectionBackoffStrategy;
    }

    public void setConnectionBackoffStrategy(final ConnectionBackoffStrategy connectionBackoffStrategy) {
        this.connectionBackoffStrategy = connectionBackoffStrategy;
    }

    public ServiceUnavailableRetryStrategy getServiceUnavailableRetryStrategy() {
        return this.serviceUnavailableRetryStrategy;
    }

    public void setServiceUnavailableRetryStrategy(final ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy) {
        this.serviceUnavailableRetryStrategy = serviceUnavailableRetryStrategy;
    }

    public Collection<? extends Header> getDefaultHeaders() {
        return this.defaultHeaders;
    }

    public void setDefaultHeaders(final Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public AuthenticationStrategy getProxyAuthenticationStrategy() {
        return this.proxyAuthenticationStrategy;
    }

    public void setProxyAuthenticationStrategy(final AuthenticationStrategy proxyAuthenticationStrategy) {
        this.proxyAuthenticationStrategy = proxyAuthenticationStrategy;
    }

    public boolean isCircularRedirectsAllowed() {
        return this.circularRedirectsAllowed;
    }

    public void setCircularRedirectsAllowed(final boolean circularRedirectsAllowed) {
        this.circularRedirectsAllowed = circularRedirectsAllowed;
    }

    public boolean isAuthenticationEnabled() {
        return this.authenticationEnabled;
    }

    public void setAuthenticationEnabled(final boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    public boolean isRedirectsEnabled() {
        return this.redirectsEnabled;
    }

    public void setRedirectsEnabled(final boolean redirectsEnabled) {
        this.redirectsEnabled = redirectsEnabled;
    }

    /**
     * Destroy.
     */
    @PreDestroy
    public void destroy() {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.executorService = null;
        }
    }

    /**
     * The type Default http client.
     */
    public static class DefaultHttpClient extends SimpleHttpClientFactoryBean {
    }



    /**
     * The type Ssl trust store aware http client.
     */
    public static class SslTrustStoreAwareHttpClient extends DefaultHttpClient {

    }
}
