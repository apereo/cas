/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.http;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

import sun.net.www.http.HttpClient;

/**
 * Based on code by Niels Campbell.
 * http://tinyurl.com/dccnc
 * 
 * @author Niels Campbell (niels_campbell_at_lycos.co.uk)
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpTimeoutClient extends HttpClient {

    /** Timeout value. */
    private static int iSoTimeout = 0;

    public HttpTimeoutClient(final URL url, final String proxy,
        final int proxyPort) throws IOException {
        super(url, proxy, proxyPort);
    }

    public HttpTimeoutClient(final URL url) throws IOException {
        this(url, null, -1);
    }

    public static HttpTimeoutClient getNew(final URL url) throws IOException {
        HttpTimeoutClient httpTimeoutClient = (HttpTimeoutClient) kac.get(url);

        if (httpTimeoutClient == null) {
            httpTimeoutClient = new HttpTimeoutClient(url); // CTOR called
            // openServer()
        } else {
            httpTimeoutClient.url = url;
        }

        return httpTimeoutClient;
    }

    public static void setSoTimeout(final int iNewSoTimeout) {
        iSoTimeout = iNewSoTimeout;
    }

    public static int getSoTimeout() {
        return iSoTimeout;
    }

    protected Socket doConnect(final String s, final int i) throws IOException {
        Socket socket = super.doConnect(s, i);

        // This is the important bit
        socket.setSoTimeout(iSoTimeout);
        return socket;
    }
}
