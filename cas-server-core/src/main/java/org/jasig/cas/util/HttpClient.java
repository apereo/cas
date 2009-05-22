/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.inspektr.common.ioc.annotation.GreaterThan;
import org.inspektr.common.ioc.annotation.NotNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class HttpClient implements Serializable {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5306738686476129516L;

    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED};

    private static final Log log = LogFactory.getLog(HttpClient.class);

    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
    @NotNull
    private int[] acceptableCodes = DEFAULT_ACCEPTABLE_CODES;

    @GreaterThan(0)
    private int connectionTimeout = 5000;

    @GreaterThan(0)
    private int readTimeout = 5000;

    @NotNull
    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
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
        final Future<Boolean> result = this.executorService.submit(new MessageSender(url, message, this.readTimeout, this.connectionTimeout));

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
            log.error(e,e);
            return false;
        }
    }

    public boolean isValidEndPoint(final URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(this.connectionTimeout);
            connection.setReadTimeout(this.readTimeout);

            connection.connect();

            final int responseCode = connection.getResponseCode();

            for (int i = 0; i < this.acceptableCodes.length; i++) {
                if (responseCode == this.acceptableCodes[i]) {
                    if (log.isDebugEnabled()) {
                        log.debug("Response code from server matched "
                            + responseCode + ".");
                    }
                    return true;
                }
            }

            if (log.isDebugEnabled()) {
                log
                    .debug("Response Code did not match any of the acceptable response codes.  Code returned was "
                        + responseCode);
            }
        } catch (final IOException e) {
            log.error(e,e);
        } finally {
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

    private static final class MessageSender implements Callable<Boolean> {

        private String url;

        private String message;

        private int readTimeout;

        private int connectionTimeout;

        public MessageSender(final String url, final String message, final int readTimeout, final int connectionTimeout) {
            this.url = url;
            this.message = message;
            this.readTimeout = readTimeout;
            this.connectionTimeout = connectionTimeout;
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
                connection.setReadTimeout(readTimeout);
                connection.setConnectTimeout(connectionTimeout);
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
                log.error(e,e);
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
