/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.ConnectionBackoffStrategy;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultBackoffStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Simple http client.
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
public final class SimpleHttpClient implements HttpClient, Serializable, DisposableBean {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5306738686476129516L;

    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED};

    private static final int MAX_POOLED_CONNECTIONS = 100;
    
    private static final int MAX_CONNECTIONS_PER_ROUTE = 50;
        
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(200);

    /** The Max pooled connections.  */
    private int maxPooledConnections = MAX_POOLED_CONNECTIONS;

    /** The Max connections per each route connections.  */
    private int maxConnectionsPerRoute = MAX_CONNECTIONS_PER_ROUTE;

    /** List of HTTP status codes considered valid by the caller. */
    @NotNull
    @Size(min = 1)
    private int[] acceptableCodes = DEFAULT_ACCEPTABLE_CODES;

    @Min(0)
    private int connectionTimeout = 5000;

    @Min(0)
    private int readTimeout = 5000;

    private RedirectStrategy redirectionStrategy = new DefaultRedirectStrategy();

    /**
     * The socket factory to be used when verifying the validity of the endpoint.
     */
    private SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();

    /**
     * The hostname verifier to be used when verifying the validity of the endpoint.
     */
    private X509HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;

    /** The credentials provider for endpoints that require authentication. */
    private CredentialsProvider credentialsProvider;

    /** The cookie store for authentication. */
    private CookieStore cookieStore;

    /** Interface for deciding whether a connection can be re-used for subsequent requests and should be kept alive. **/
    private ConnectionReuseStrategy connectionReuseStrategy = new DefaultConnectionReuseStrategy();

    /**
     * When managing a dynamic number of connections for a given route, this strategy assesses whether a
     * given request execution outcome should result in a backoff
     * signal or not, based on either examining the Throwable that resulted or by examining
     * the resulting response (e.g. for its status code).
     */
    private ConnectionBackoffStrategy connectionBackoffStrategy = new DefaultBackoffStrategy();

    /** Strategy interface that allows API users to plug in their own logic to control whether or not a retry
     * should automatically be done, how many times it should be retried and so on.
     */
    private ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = new DefaultServiceUnavailableRetryStrategy();

    /** Default headers to be sent. **/
    private Collection<? extends Header> defaultHeaders = Collections.emptyList();

    /** Default strategy implementation for proxy host authentication.**/
    private AuthenticationStrategy proxyAuthenticationStrategy = new ProxyAuthenticationStrategy();

    /** Determines whether circular redirects (redirects to the same location) should be allowed. **/
    private boolean circularRedirectsAllowed = true;

    /** Determines whether authentication should be handled automatically. **/
    private boolean authenticationEnabled = false;

    /** Determines whether redirects should be handled automatically. **/
    private boolean redirectsEnabled = true;

    private CloseableHttpClient httpClient = null;

    /**
     * Instantiates a new Simple http client.
     */
    public SimpleHttpClient() {
        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param acceptableCodes the acceptable codes
     */
    public SimpleHttpClient(final int[] acceptableCodes) {
        this.acceptableCodes = acceptableCodes;
        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param sslSocketFactory the ssl socket factory
     */
    public SimpleHttpClient(final SSLConnectionSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param readTimeout the read timeout
     * @param connectionTimeout the connection timeout
     */
    public SimpleHttpClient(final int readTimeout, final int connectionTimeout) {
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param redirectsEnabled the redirects enabled
     * @param circularRedirectsAllowed the circular redirects allowed
     */
    public SimpleHttpClient(final boolean redirectsEnabled, final boolean circularRedirectsAllowed) {
        this.redirectsEnabled = redirectsEnabled;
        this.circularRedirectsAllowed = circularRedirectsAllowed;
        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param redirectsEnabled the redirects enabled
     * @param readTimeout the read timeout
     * @param connectionTimeout the connection timeout
     * @param circularRedirectsAllowed the circular redirects allowed
     */
    public SimpleHttpClient(final boolean redirectsEnabled, final int readTimeout,
                            final int connectionTimeout, final boolean circularRedirectsAllowed) {
        this.redirectsEnabled = redirectsEnabled;
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        this.circularRedirectsAllowed = circularRedirectsAllowed;

        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param sslSocketFactory the ssl socket factory
     * @param hostnameVerifier the hostname verifier
     * @param acceptableCodes the acceptable codes
     */
    public SimpleHttpClient(final SSLConnectionSocketFactory sslSocketFactory,
                            final X509HostnameVerifier hostnameVerifier, final int[] acceptableCodes) {
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.acceptableCodes = acceptableCodes;
        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param sslSocketFactory the ssl socket factory
     * @param readTimeout the read timeout
     * @param connectionTimeout the connection timeout
     */
    public SimpleHttpClient(final SSLConnectionSocketFactory sslSocketFactory, final int readTimeout, final int connectionTimeout) {
        this.sslSocketFactory = sslSocketFactory;
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        init();
    }

    /**
     * Instantiates a new Simple http client.
     *
     * @param executorService the executor service
     * @param acceptableCodes the acceptable codes
     * @param connectionTimeout the connection timeout
     * @param readTimeout the read timeout
     * @param redirectionStrategy the redirection strategy
     * @param sslSocketFactory the ssl socket factory
     * @param hostnameVerifier the hostname verifier
     * @param credentialsProvider the credentials provider
     * @param cookieStore the cookie store
     * @param connectionReuseStrategy the connection reuse strategy
     * @param connectionBackoffStrategy the connection backoff strategy
     * @param serviceUnavailableRetryStrategy the service unavailable retry strategy
     * @param defaultHeaders the default headers
     * @param proxyAuthenticationStrategy the proxy authentication strategy
     * @param circularRedirectsAllowed the circular redirects allowed
     * @param authenticationEnabled the authentication enabled
     * @param redirectsEnabled the redirects enabled
     * @param maxConnections the max connections
     * @param maxConnectionsPerRoute the max connections per route
     */
    public SimpleHttpClient(final ExecutorService executorService, final int[] acceptableCodes,
                            final int connectionTimeout, final int readTimeout,
                            final RedirectStrategy redirectionStrategy,
                            final SSLConnectionSocketFactory sslSocketFactory,
                            final X509HostnameVerifier hostnameVerifier,
                            final CredentialsProvider credentialsProvider,
                            final CookieStore cookieStore, final ConnectionReuseStrategy connectionReuseStrategy,
                            final ConnectionBackoffStrategy connectionBackoffStrategy,
                            final ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy,
                            final Collection<? extends Header> defaultHeaders,
                            final AuthenticationStrategy proxyAuthenticationStrategy,
                            final boolean circularRedirectsAllowed,
                            final boolean authenticationEnabled,
                            final boolean redirectsEnabled,
                            final int maxConnections,
                            final int maxConnectionsPerRoute) {
        this.executorService = executorService;
        this.acceptableCodes = acceptableCodes;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.redirectionStrategy = redirectionStrategy;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.credentialsProvider = credentialsProvider;
        this.cookieStore = cookieStore;
        this.connectionReuseStrategy = connectionReuseStrategy;
        this.connectionBackoffStrategy = connectionBackoffStrategy;
        this.serviceUnavailableRetryStrategy = serviceUnavailableRetryStrategy;
        this.defaultHeaders = defaultHeaders;
        this.proxyAuthenticationStrategy = proxyAuthenticationStrategy;
        this.circularRedirectsAllowed = circularRedirectsAllowed;
        this.authenticationEnabled = authenticationEnabled;
        this.redirectsEnabled = redirectsEnabled;
        this.maxPooledConnections = maxConnections;
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;

        init();
    }

    @Override
    public boolean sendMessageToEndPoint(@NotNull final HttpMessage message) {
        Assert.notNull(this.httpClient);

        FutureRequestExecutionService service = null;
        try {
            final HttpPost request = new HttpPost(message.getUrl().toURI());
            request.addHeader("Content-Type", message.getContentType());
            
            final StringEntity entity = new StringEntity(message.getMessage(), ContentType.create(message.getContentType()));
            request.setEntity(entity);

            service = new FutureRequestExecutionService(this.httpClient, executorService);

            final HttpRequestFutureTask<String> task = service.execute(request,
                    HttpClientContext.create(), new BasicResponseHandler());

            if (message.isAsynchronous()) {
                return true;
            }
            
            return StringUtils.isNotBlank(task.get());
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return false;
        } finally {
            IOUtils.closeQuietly(service);
        }
    }
        
    @Override
    public boolean isValidEndPoint(final String url) {
        try {
            final URL u = new URL(url);
            return isValidEndPoint(u);
        } catch (final MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isValidEndPoint(final URL url) {
        Assert.notNull(this.httpClient);

        CloseableHttpResponse response = null;
        HttpEntity entity = null;

        try {
            final HttpGet request = new HttpGet(url.toURI());

            response = this.httpClient.execute(request);
            final int responseCode = response.getStatusLine().getStatusCode();

            for (final int acceptableCode : this.acceptableCodes) {
                if (responseCode == acceptableCode) {
                    LOGGER.debug("Response code from server matched {}.", responseCode);
                    return true;
                }
            }

            LOGGER.debug("Response code did not match any of the acceptable response codes. Code returned was {}",
                    responseCode);

            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                final String value = response.getStatusLine().getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: {}; The error was:\n{}", url.toExternalForm(),
                        value);
            }

            entity = response.getEntity();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            EntityUtils.consumeQuietly(entity);
            IOUtils.closeQuietly(response);
           
        }
        return false;
    }

    /**
     * Shutdown the executor service and close the http client.
     * @throws Exception if the executor cannot properly shut down
     */
    public void destroy() throws Exception {
        executorService.shutdown();
        IOUtils.closeQuietly(this.httpClient);
    }

    /**
     * @deprecated As of 4.1
     * Note that changing this executor will affect all httpClients.  While not ideal, this change
     * was made because certain ticket registries
     * were persisting the HttpClient and thus getting serializable exceptions.
     * @param executorService The executor service to send messages to end points.
     */
    @Deprecated
    public void setExecutorService(@NotNull final ExecutorService executorService) {
        LOGGER.warn("setExecutorService() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1
     * Set the acceptable HTTP status codes that we will use to determine if the
     * response from the URL was correct.
     *
     * @param acceptableCodes an array of status code integers.
     */
    @Deprecated
    public void setAcceptableCodes(final int[] acceptableCodes) {
        LOGGER.warn("setAcceptableCodes() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1
     * Sets a specified timeout value, in milliseconds, to be used when opening the endpoint url.
     * @param connectionTimeout specified timeout value in milliseconds
     */
    @Deprecated
    public void setConnectionTimeout(final int connectionTimeout) {
        LOGGER.warn("setConnectionTimeout() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1
     * Sets a specified timeout value, in milliseconds, to be used when reading from the endpoint url.
     * @param readTimeout specified timeout value in milliseconds
     */
    @Deprecated
    public void setReadTimeout(final int readTimeout) {
        LOGGER.warn("setReadTimeout() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1
     * Determines the behavior on receiving 3xx responses from HTTP endpoints.
     *
     * @param follow True to follow 3xx redirects (default), false otherwise.
     */
    @Deprecated
    public void setFollowRedirects(final boolean follow) {
        LOGGER.warn("setFollowRedirects() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1
     * Set the SSL socket factory be used by the URL when submitting
     * request to check for URL endpoint validity.
     * @param factory ssl socket factory instance to use
     * @see #isValidEndPoint(URL)
     */
    @Deprecated
    public void setSSLSocketFactory(final SSLSocketFactory factory) {
        LOGGER.warn("setSSLSocketFactory() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1
     * Set the hostname verifier be used by the URL when submitting
     * request to check for URL endpoint validity.
     * @param verifier hostname verifier instance to use
     * @see #isValidEndPoint(URL)
     */
    @Deprecated
    public void setHostnameVerifier(final HostnameVerifier verifier) {
        LOGGER.warn("setHostnameVerifier() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * Prepare the http client with configured settings.
     */
    private void init() {
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

            final HttpHost httpHost = new HttpHost(InetAddress.getLocalHost());
            final HttpRoute httpRoute = new HttpRoute(httpHost);
            connMgmr.setMaxPerRoute(httpRoute, MAX_CONNECTIONS_PER_ROUTE);
    
            final RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(this.readTimeout)
                    .setConnectTimeout(this.connectionTimeout)
                    .setConnectionRequestTimeout(this.connectionTimeout)
                    .setStaleConnectionCheckEnabled(true)
                    .setCircularRedirectsAllowed(this.circularRedirectsAllowed)
                    .setRedirectsEnabled(this.redirectsEnabled)
                    .setAuthenticationEnabled(this.authenticationEnabled)
                    .build();
            
            final HttpClientBuilder builder = HttpClients.custom()
                    .setConnectionManager(connMgmr)
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(this.sslSocketFactory)
                    .setHostnameVerifier(this.hostnameVerifier)
                    .setRedirectStrategy(this.redirectionStrategy)
                    .setDefaultCredentialsProvider(this.credentialsProvider)
                    .setDefaultCookieStore(this.cookieStore)
                    .setConnectionReuseStrategy(this.connectionReuseStrategy)
                    .setConnectionBackoffStrategy(this.connectionBackoffStrategy)
                    .setServiceUnavailableRetryStrategy(this.serviceUnavailableRetryStrategy)
                    .setProxyAuthenticationStrategy(this.proxyAuthenticationStrategy)
                    .setDefaultHeaders(this.defaultHeaders)
                    .useSystemProperties();


            this.httpClient = builder.build();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
