/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities class for generic functions related to URLs.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class UrlUtils {

    /** The instance of the logger. */
    private static final Log LOG = LogFactory.getLog(UrlUtils.class);

    private UrlUtils() {
        // we do not want this able to be extended.
    }

    public static int getResponseCodeFromString(final String url) {
        try {
            return getResponseCodeFromUrl(new URL(url));
        } catch (Exception e) {
            LOG.error(e, e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    public static int getResponseCodeFromUrl(final URL url) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();

            connection.setRequestProperty("Connection", "close");
            return connection.getResponseCode();
        } catch (SSLHandshakeException e) {
            LOG.error(e, e);
            LOG
                .error("This exception is generally an indication that your JVM keystore does not trust the server certificate being returned by the server at the URL: "
                    + url.toExternalForm());
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception e) {
            LOG.error(e, e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }
}
