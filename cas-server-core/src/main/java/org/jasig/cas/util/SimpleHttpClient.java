/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

/**
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

    private static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(MAX_POOLED_CONNECTIONS);

    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
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
     *
     * @see #setSSLSocketFactory(SSLSocketFactory)
     */
    private SSLConnectionSocketFactory sslSocketFactory = null;

    /**
     * The hostname verifier to be used when verifying the validity of the endpoint.
     *
     * @see #setHostnameVerifier(HostnameVerifier)
     */
    private X509HostnameVerifier hostnameVerifier = null;

    private CloseableHttpClient httpClient = null;

    /** The credentials provider for endpoints that require authentication. */
    private CredentialsProvider credentialsProvider;

    /** The cookie store for authentication. */
    private CookieStore cookieStore;
    
    /**
     * Note that changing this executor will affect all httpClients.  While not ideal, this change
     * was made because certain ticket registries
     * were persisting the HttpClient and thus getting serializable exceptions.
     * @param executorService The executor service to send messages to end points.
     */
    public void setExecutorService(@NotNull final ExecutorService executorService) {
        Assert.notNull(executorService);
        EXECUTOR_SERVICE = executorService;
    }

    public void setCookieStore(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }
    
    public void setCredentialsProvider(@NotNull final CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }
    
    @Override
    public boolean sendMessageToEndPoint(@NotNull final HttpMessage message) {
        FutureRequestExecutionService service = null;
        try {
            final HttpPost request = new HttpPost(message.getUrl().toURI());
            request.addHeader("Content-Length", Integer.toString(message.getMessage().getBytes().length));
            request.addHeader("Content-Type", message.getContentType());
            
            final StringEntity entity = new StringEntity(message.getMessage(), message.getContentType());
            request.setEntity(entity);
            
            service = new FutureRequestExecutionService(this.httpClient, EXECUTOR_SERVICE);
 
            final HttpRequestFutureTask<String> task = service.execute(request,
                    HttpClientContext.create(), new BasicResponseHandler()); 
                    
            if (message.isAsynchronous()) {
                return true;
            }
            
            return StringUtils.isNotBlank(task.get());
        } catch (final Exception e) {
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
        CloseableHttpResponse response = null;
               
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
           
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
            IOUtils.closeQuietly(response);
           
        }
        return false;
    }

    /**
     * Set the acceptable HTTP status codes that we will use to determine if the
     * response from the URL was correct.
     *
     * @param acceptableCodes an array of status code integers.
     */
    public void setAcceptableCodes(final int[] acceptableCodes) {
        this.acceptableCodes = acceptableCodes;
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening the endpoint url.
     * @param connectionTimeout specified timeout value in milliseconds
     */
    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when reading from the endpoint url.
     * @param readTimeout specified timeout value in milliseconds
     */
    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Determines the behavior on receiving 3xx responses from HTTP endpoints.
     *
     * @param follow True to follow 3xx redirects (default), false otherwise.
     */
    public void setRedirectionStrategy(final RedirectStrategy follow) {
        this.redirectionStrategy = follow;
    }

    /**
     * Set the SSL socket factory be used by the URL when submitting
     * request to check for URL endpoint validity.
     * @param factory ssl socket factory instance to use
     * @see #isValidEndPoint(URL)
     */
    public void setSSLSocketFactory(final SSLConnectionSocketFactory factory) {
        this.sslSocketFactory = factory;
    }

    /**
     * Set the hostname verifier be used by the URL when submitting
     * request to check for URL endpoint validity.
     * @param verifier hostname verifier instance to use
     * @see #isValidEndPoint(URL)
     */
    public void setHostnameVerifier(final X509HostnameVerifier verifier) {
        this.hostnameVerifier = verifier;
    }

    /**
     * Shutdown the executor service and close the http client.
     * @throws Exception if the executor cannot properly shut down
     */
    public void destroy() throws Exception {
        EXECUTOR_SERVICE.shutdown();
        IOUtils.closeQuietly(this.httpClient);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    /**
     * Prepare the http client with configured settings.
     *
     * @return the http client
     */
    @PostConstruct
    protected CloseableHttpClient initializeHttpClient() {
        try {
            final PoolingHttpClientConnectionManager connMgmr = new PoolingHttpClientConnectionManager();
            connMgmr.setMaxTotal(MAX_POOLED_CONNECTIONS);
            connMgmr.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
            
            final HttpHost httpHost = new HttpHost(InetAddress.getLocalHost());
            final HttpRoute httpRoute = new HttpRoute(httpHost);
            connMgmr.setMaxPerRoute(httpRoute, MAX_CONNECTIONS_PER_ROUTE);
    
            final RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(this.readTimeout)
                    .setConnectTimeout(this.connectionTimeout)
                    .setConnectionRequestTimeout(this.connectionTimeout)
                    .setStaleConnectionCheckEnabled(true)
                    .setCircularRedirectsAllowed(true)
                    .build();
            
            final HttpClientBuilder builder = HttpClients.custom()
                    .setSSLSocketFactory(this.sslSocketFactory)
                    .setHostnameVerifier(this.hostnameVerifier)
                    .setRedirectStrategy(this.redirectionStrategy)
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(connMgmr)
                    .setDefaultCredentialsProvider(this.credentialsProvider)
                    .setDefaultCookieStore(this.cookieStore)
                    .useSystemProperties();
                    
            this.httpClient  = builder.build();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
