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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class HttpClient implements Serializable, DisposableBean {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5306738686476129516L;

    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED};

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    private static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(100);


    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
    @NotNull
    @Size(min=1)
    private int[] acceptableCodes = DEFAULT_ACCEPTABLE_CODES;

    @Min(0)
    private int connectionTimeout = 5000;

    @Min(0)
    private int readTimeout = 5000;

    private boolean followRedirects = true;


    /**
     * Note that changing this executor will affect all httpClients.  While not ideal, this change was made because certain ticket registries
     * were persisting the HttpClient and thus getting serializable exceptions.
     * @param executorService
     */
    public void setExecutorService(final ExecutorService executorService) {
        Assert.notNull(executorService);
        EXECUTOR_SERVICE = executorService;
    }

    /**
     * Sends a message to a particular endpoint.  Option of sending it without waiting to ensure a response was returned.
     * <p>
     * This is useful when it doesn't matter about the response as you'll perform no action based on the response.
     *
     * @param url the url to send the message to
     * @param message the message itself
     * @param async true if you don't want to wait for the response, false otherwise.
     * @return boolean if the message was sent, or async was used.  false if the message failed.
     */
    public boolean sendMessageToEndPoint(final String url, final String message, final boolean async) {
        final Future<Boolean> result = EXECUTOR_SERVICE.submit(new MessageSender(url, message, this.readTimeout, this.connectionTimeout, this.followRedirects));

        if (async) {
            return true;
        }

        try {
            return result.get();
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isValidEndPoint(final String url) {
        try {
            final URL u = new URL(url);
            return isValidEndPoint(u);
        } catch (final MalformedURLException e) {
            log.error(e.getMessage(),e);
            return false;
        }
    }

    public boolean isValidEndPoint(final URL url) {
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(this.connectionTimeout);
            connection.setReadTimeout(this.readTimeout);
            connection.setInstanceFollowRedirects(this.followRedirects);

            connection.connect();

            final int responseCode = connection.getResponseCode();

            for (final int acceptableCode : this.acceptableCodes) {
                if (responseCode == acceptableCode) {
                    if (log.isDebugEnabled()) {
                        log.debug("Response code from server matched " + responseCode + ".");
                    }
                    return true;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Response Code did not match any of the acceptable response codes.  Code returned was " + responseCode);
            }

            // if the response code is an error and we don't find that error acceptable above:
            if (responseCode == 500) {
                is = connection.getInputStream();
                final String value = IOUtils.toString(is);
                log.error(String.format("There was an error contacting the endpoint: %s; The error was:\n%s", url.toExternalForm(), value));
            }
        } catch (final IOException e) {
            log.error(e.getMessage(),e);
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
    public final void setAcceptableCodes(final int[] acceptableCodes) {
        this.acceptableCodes = acceptableCodes;
    }

    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

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

    public void destroy() throws Exception {
        EXECUTOR_SERVICE.shutdown();
    }

    private static final class MessageSender implements Callable<Boolean> {

        private String url;

        private String message;

        private int readTimeout;

        private int connectionTimeout;

        private boolean followRedirects;

        public MessageSender(final String url, final String message, final int readTimeout, final int connectionTimeout, final boolean followRedirects) {
            this.url = url;
            this.message = message;
            this.readTimeout = readTimeout;
            this.connectionTimeout = connectionTimeout;
            this.followRedirects = followRedirects;
        }

        public Boolean call() throws Exception {
            HttpURLConnection connection = null;
            BufferedReader in = null;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to access " + url);
                }
                final URL logoutUrl = new URL(url);
                final String output = "logoutRequest=" + URLEncoder.encode(message, "UTF-8");

                connection = (HttpURLConnection) logoutUrl.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setReadTimeout(this.readTimeout);
                connection.setConnectTimeout(this.connectionTimeout);
                connection.setInstanceFollowRedirects(this.followRedirects);
                connection.setRequestProperty("Content-Length", Integer.toString(output.getBytes().length));
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                final DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
                printout.writeBytes(output);
                printout.flush();
                printout.close();

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                while (in.readLine() != null) {
                    // nothing to do
                }

                if (log.isDebugEnabled()) {
                    log.debug("Finished sending message to" + url);
                }
                return true;
            } catch (final SocketTimeoutException e) {
                log.warn("Socket Timeout Detected while attempting to send message to [" + url + "].");
                return false;
            } catch (final Exception e) {
                log.warn("Error Sending message to url endpoint [" + url + "].  Error is [" + e.getMessage() + "]");
                return false;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (final IOException e) {
                        // can't do anything
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

    }
}
