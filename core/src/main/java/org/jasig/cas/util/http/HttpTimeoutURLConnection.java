/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.http;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import sun.net.www.http.HttpClient;

// Need to override any function in HttpURLConnection that create a new
// HttpClient
// and create a HttpTimeoutClient instead. Those functions are
// connect(), getNewClient(), getProxiedClient()

/**
 * Based on code by Niels Campbell.
 * http://tinyurl.com/dccnc
 * 
 * @author Niels Campbell (niels_campbell_at_lycos.co.uk)
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpTimeoutURLConnection extends
    sun.net.www.protocol.http.HttpURLConnection {

    public HttpTimeoutURLConnection(final URL u,
        final HttpTimeoutHandler handler, final int iSoTimeout)
        throws IOException {
        super(u, handler);
        HttpTimeoutClient.setSoTimeout(iSoTimeout);
    }

    public void connect() throws IOException {
        if (this.connected) {
            return;
        }

        try {
            // && !failedOnce <-PRIVATE
            if ("http".equals(this.url.getProtocol())) {
                // for safety's sake, as reported by KLGroup
                synchronized (this.url) {
                    this.http = HttpTimeoutClient.getNew(this.url);
                }
            } else {
                if (this.handler instanceof HttpTimeoutHandler) {
                    this.http = new HttpTimeoutClient(super.url,
                        ((HttpTimeoutHandler) this.handler).getProxy(),
                        ((HttpTimeoutHandler) this.handler).getProxyPort());
                } else {
                    throw new IOException("HttpTimeoutHandler expected");
                }
            }

            this.ps = (PrintStream) this.http.getOutputStream();
        } catch (IOException e) {
            throw e;
        }

        this.connected = true;
    }

    protected HttpClient getNewClient(final URL url) throws IOException {
        HttpTimeoutClient httpTimeoutClient = new HttpTimeoutClient(url,
            (String) null, -1);
        return httpTimeoutClient;
    }

    protected HttpClient getProxiedClient(final URL url, final String s,
        final int i) throws IOException {
        HttpTimeoutClient httpTimeoutClient = new HttpTimeoutClient(url, s, i);
        return httpTimeoutClient;
    }
}
