package org.apereo.cas.util.http;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.ConnectionBackoffStrategy;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultBackoffStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

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
@Setter
public class SimpleHttpClientFactoryBean implements FactoryBean<SimpleHttpClient>, DisposableBean {

    /**
     * Max connections per route.
     */
    public static final int MAX_CONNECTIONS_PER_ROUTE = 50;

    private static final int MAX_POOLED_CONNECTIONS = 100;

    private static final int DEFAULT_THREADS_NUMBER = 200;

    private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * The default status codes we accept.
     */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[]{HttpURLConnection.HTTP_OK,
        HttpURLConnection.HTTP_NOT_MODIFIED, HttpURLConnection.HTTP_MOVED_TEMP,
        HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_ACCEPTED,
        HttpURLConnection.HTTP_NO_CONTENT};

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

    /**
     * The redirection strategy by default, using http status codes.
     */
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
     * Design a retry strategy for http requests. Default appears to be 3 retries.
     */
    private HttpRequestRetryHandler httpRequestRetryHandler = new DefaultHttpRequestRetryHandler();

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
        val httpClient = buildHttpClient();
        val requestExecutorService = buildRequestExecutorService(httpClient);
        val codes = this.acceptableCodes.stream().sorted().collect(Collectors.toList());
        return new SimpleHttpClient(codes, httpClient, requestExecutorService);
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
    @SneakyThrows
    private CloseableHttpClient buildHttpClient() {
        val plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        val registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", plainSocketFactory)
            .register("https", this.sslSocketFactory)
            .build();

        val connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(this.maxPooledConnections);
        connectionManager.setDefaultMaxPerRoute(this.maxConnectionsPerRoute);
        connectionManager.setValidateAfterInactivity(DEFAULT_TIMEOUT);

        val httpHost = new HttpHost(InetAddress.getLocalHost());
        val httpRoute = new HttpRoute(httpHost);
        connectionManager.setMaxPerRoute(httpRoute, MAX_CONNECTIONS_PER_ROUTE);

        val requestConfig = RequestConfig.custom()
            .setSocketTimeout(this.readTimeout)
            .setConnectTimeout((int) this.connectionTimeout)
            .setConnectionRequestTimeout((int) this.connectionTimeout)
            .setCircularRedirectsAllowed(this.circularRedirectsAllowed)
            .setRedirectsEnabled(this.redirectsEnabled)
            .setAuthenticationEnabled(this.authenticationEnabled)
            .build();

        val builder = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .setSSLSocketFactory(this.sslSocketFactory)
            .setSSLHostnameVerifier(this.hostnameVerifier)
            .setRedirectStrategy(this.redirectionStrategy)
            .setDefaultCredentialsProvider(this.credentialsProvider)
            .setDefaultCookieStore(this.cookieStore)
            .setConnectionReuseStrategy(this.connectionReuseStrategy)
            .setConnectionBackoffStrategy(this.connectionBackoffStrategy)
            .setServiceUnavailableRetryStrategy(this.serviceUnavailableRetryStrategy)
            .setProxyAuthenticationStrategy(this.proxyAuthenticationStrategy)
            .setDefaultHeaders(this.defaultHeaders)
            .setRetryHandler(this.httpRequestRetryHandler)
            .useSystemProperties();
        return builder.build();
    }

    /**
     * Build a {@link FutureRequestExecutionService} from the current properties and a HTTP client.
     *
     * @param httpClient the provided HTTP client
     * @return the built request executor service
     */
    private FutureRequestExecutionService buildRequestExecutorService(final CloseableHttpClient httpClient) {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(this.threadsNumber, this.threadsNumber, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(this.queueSize));
        }
        return new FutureRequestExecutionService(httpClient, this.executorService);
    }

    /**
     * Destroy the service if available.
     */
    @Override
    public void destroy() {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.executorService = null;
        }
    }

    /**
     * The default http client.
     */
    public static class DefaultHttpClient extends SimpleHttpClientFactoryBean {
    }
}
