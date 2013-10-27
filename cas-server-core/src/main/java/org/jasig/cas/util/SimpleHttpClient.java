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


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public final class SimpleHttpClient implements HttpClient<Boolean>, Serializable, DisposableBean {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5306738686476129516L;

    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED};

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);

    private static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(100);

    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
    @NotNull
    @Size(min = 1)
    private int[] acceptableCodes = DEFAULT_ACCEPTABLE_CODES;

    @Min(0)
    private int connectionTimeout = 5000;

    @Min(0)
    private int readTimeout = 5000;

    private boolean followRedirects = true;

    /**
     * The socket factory to be used when verifying the validity of the endpoint.
     *
     * @see #setSSLSocketFactory(SSLSocketFactory)
     */
    private SSLSocketFactory sslSocketFactory = null;

    /**
     * The hostname verifier to be used when verifying the validity of the endpoint.
     *
     * @see #setHostnameVerifier(HostnameVerifier)
     */
    private HostnameVerifier hostnameVerifier = null;

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

    @Override
    public boolean sendMessageToEndPoint(final boolean async, final CallableMessageSender task) {
        task.setConnectionTimeout(this.connectionTimeout);
        task.setFollowRedirects(this.followRedirects);
        task.setReadTimeout(this.readTimeout);
        
        final Future<Boolean> result = EXECUTOR_SERVICE.submit(task);

        if (async) {
            return true;
        }

        try {
            return result.get();
        } catch (final Exception e) {
            return false;
        }
    }
    
    /**
     * Uses an instance of {@link CallableMessageSender} by default as the executed task
     * to send the message to the endpoint.
     * @param url url to send the message to
     * @param async whether to send the message in async fashion
     * @param message message to send
     * @see #sendMessageToEndPoint(String, String, boolean)
     * @return boolean if the message was sent, or async was used. false if the message failed
     */
    public boolean sendMessageToEndPoint(final String url, final String message, final boolean async) {
        return sendMessageToEndPoint(async, new CallableMessageSender(url, message));
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
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(this.connectionTimeout);
            connection.setReadTimeout(this.readTimeout);
            connection.setInstanceFollowRedirects(this.followRedirects);

            if (connection instanceof HttpsURLConnection) {
                final HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

                if (this.sslSocketFactory != null) {
                    httpsConnection.setSSLSocketFactory(this.sslSocketFactory);
                }

                if (this.hostnameVerifier != null) {
                    httpsConnection.setHostnameVerifier(this.hostnameVerifier);
                }
            }

            connection.connect();

            final int responseCode = connection.getResponseCode();

            for (final int acceptableCode : this.acceptableCodes) {
                if (responseCode == acceptableCode) {
                    LOGGER.debug("Response code from server matched {}.", responseCode);
                    return true;
                }
            }

            LOGGER.debug("Response Code did not match any of the acceptable response codes. Code returned was {}",
                    responseCode);

            // if the response code is an error and we don't find that error acceptable above:
            if (responseCode == 500) {
                is = connection.getInputStream();
                final String value = IOUtils.toString(is);
                LOGGER.error("There was an error contacting the endpoint: {}; The error was:\n{}", url.toExternalForm(),
                        value);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
            if (connection != null) {
                connection.disconnect();
            }
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
    public void setFollowRedirects(final boolean follow) {
        this.followRedirects = follow;
    }

    /**
     * Set the SSL socket factory be used by the URL when submitting
     * request to check for URL endpoint validity.
     * @param factory ssl socket factory instance to use
     * @see #isValidEndPoint(URL)
     */
    public void setSSLSocketFactory(final SSLSocketFactory factory) {
        this.sslSocketFactory = factory;
    }

    /**
     * Set the hostname verifier be used by the URL when submitting
     * request to check for URL endpoint validity.
     * @param verifier hostname verifier instance to use
     * @see #isValidEndPoint(URL)
     */
    public void setHostnameVerifier(final HostnameVerifier verifier) {
        this.hostnameVerifier = verifier;
    }

    /**
     * Shutdown the executor service.
     * @throws Exception if the executor cannot properly shut down
     */
    public void destroy() throws Exception {
        EXECUTOR_SERVICE.shutdown();
    }

}
