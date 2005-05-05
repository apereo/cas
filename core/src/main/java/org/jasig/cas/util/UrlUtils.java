/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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

    /**
     * Method to retrieve the text response from a HTTP request for a specific URL.
     * 
     * <p>Warning, because we are constrained to support Java 1.4, 
     * this routine can hang for an indefinite period of time. Until
     * J2SE 5, there is no way to make a read from a URL connection 
     * timeout.</p>
     * 
     * @param url The URL to contact.
     * @return the body of the response.
     */
    public static String getResponseBodyFromUrl(final URL url) {
        BufferedReader bufferedReader = null;
        final StringBuffer buf = new StringBuffer();

        URLConnection connection = null;
        try {
            connection = url.openConnection();
            
            /* connection.setReadTimeout(60000);    [J2SE 5.0] */
            /* connection.setConnectTimeout(60000); [J2SE 5.0] */
            connection.setRequestProperty("Connection", "close");
            bufferedReader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
            String line = bufferedReader.readLine();
            while (line != null) {
                buf.append(line);
                buf.append("\n");
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                LOG.error(e);
            }
        }
        return buf.toString().length() > 0 ? buf.toString() : null;
    }
}
