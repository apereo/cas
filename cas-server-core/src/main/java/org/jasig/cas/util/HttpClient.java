/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.util.annotation.GreaterThan;
import org.jasig.cas.util.annotation.NotNull;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class HttpClient {

    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED};

    private Log log = LogFactory.getLog(this.getClass());

    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
    @NotNull
    private int[] acceptableCodes = DEFAULT_ACCEPTABLE_CODES;

    @GreaterThan(0)
    private int connectionTimeout = 5000;

    @GreaterThan(0)
    private int readTimeout = 5000;

    public boolean isValidEndPoint(final String url) {
        try {
            final URL u = new URL(url);
            return isValidEndPoint(u);
        } catch (final MalformedURLException e) {
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
            // nothing to do
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
}
