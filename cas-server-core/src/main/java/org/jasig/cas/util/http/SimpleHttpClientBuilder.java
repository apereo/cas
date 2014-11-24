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
package org.jasig.cas.util.http;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The builder dedicated to {@link SimpleHttpClient}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
public final class SimpleHttpClientBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClientBuilder.class);

    /**
     * Build a HTTP client from a given configuration.
     *
     * @param configuration the provided configuration
     * @return the built HTTP client
     */
    public CloseableHttpClient buildHttpClient(final SimpleHttpClientConfiguration configuration) {
        try {

            final ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
            final LayeredConnectionSocketFactory sslsf = configuration.getSslSocketFactory();

            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", plainsf)
                    .register("https", sslsf)
                    .build();

            final PoolingHttpClientConnectionManager connMgmr = new PoolingHttpClientConnectionManager(registry);
            connMgmr.setMaxTotal(configuration.getMaxPooledConnections());
            connMgmr.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());

            final HttpHost httpHost = new HttpHost(InetAddress.getLocalHost());
            final HttpRoute httpRoute = new HttpRoute(httpHost);
            connMgmr.setMaxPerRoute(httpRoute, SimpleHttpClientConfiguration.MAX_CONNECTIONS_PER_ROUTE);
    
            final RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(configuration.getReadTimeout())
                    .setConnectTimeout(configuration.getConnectionTimeout())
                    .setConnectionRequestTimeout(configuration.getConnectionTimeout())
                    .setStaleConnectionCheckEnabled(true)
                    .setCircularRedirectsAllowed(configuration.isCircularRedirectsAllowed())
                    .setRedirectsEnabled(configuration.isRedirectsEnabled())
                    .setAuthenticationEnabled(configuration.isAuthenticationEnabled())
                    .build();
            
            final HttpClientBuilder builder = HttpClients.custom()
                    .setConnectionManager(connMgmr)
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(sslsf)
                    .setHostnameVerifier(configuration.getHostnameVerifier())
                    .setRedirectStrategy(configuration.getRedirectionStrategy())
                    .setDefaultCredentialsProvider(configuration.getCredentialsProvider())
                    .setDefaultCookieStore(configuration.getCookieStore())
                    .setConnectionReuseStrategy(configuration.getConnectionReuseStrategy())
                    .setConnectionBackoffStrategy(configuration.getConnectionBackoffStrategy())
                    .setServiceUnavailableRetryStrategy(configuration.getServiceUnavailableRetryStrategy())
                    .setProxyAuthenticationStrategy(configuration.getProxyAuthenticationStrategy())
                    .setDefaultHeaders(configuration.getDefaultHeaders())
                    .useSystemProperties();

            return builder.build();

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Build a {@link FutureRequestExecutionService} from a provided configuration and a HTTP client.
     *
     * @param configuration the provided configuration
     * @param httpClient the provided HTTP client
     * @return the built request executor service
     */
    public FutureRequestExecutionService buildRequestExecutorService(final SimpleHttpClientConfiguration configuration,
            final CloseableHttpClient httpClient) {

        final ExecutorService configurationExecutorService = configuration.getExecutorService();
        final ExecutorService definedExecutorService;
        // no executor service provided -> create a default one
        if (configurationExecutorService == null) {
            final int threadsNumber = configuration.getThreadsNumber();
            definedExecutorService = new ThreadPoolExecutor(threadsNumber, threadsNumber,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(configuration.getQueueSize()));
        } else {
            definedExecutorService = configurationExecutorService;
        }

        return new FutureRequestExecutionService(httpClient, definedExecutorService);
    }
}
