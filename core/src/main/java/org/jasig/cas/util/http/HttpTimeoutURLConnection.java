package org.jasig.cas.util.http;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import sun.net.www.http.HttpClient;

// Need to override any function in HttpURLConnection that create a new HttpClient
// and create a HttpTimeoutClient instead. Those functions are
// connect(), getNewClient(), getProxiedClient()

/**
 * Based on code from:
 * http://coding.derkeiler.com/Archive/Java/comp.lang.java.programmer/2004-01/3271.html
 */
public class HttpTimeoutURLConnection extends
    sun.net.www.protocol.http.HttpURLConnection {

    public HttpTimeoutURLConnection(URL u, HttpTimeoutHandler handler,
        int iSoTimeout) throws IOException {
        super(u, handler);
        HttpTimeoutClient.setSoTimeout(iSoTimeout);
    }

    public void connect() throws IOException {
        if (this.connected) {
            return;
        }

        try {
            if ("http".equals(this.url.getProtocol())) // && !failedOnce <-PRIVATE
            {
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

    protected HttpClient getNewClient(URL url) throws IOException {
        HttpTimeoutClient httpTimeoutClient = new HttpTimeoutClient(url,
            (String) null, -1);
        return httpTimeoutClient;
    }

    protected HttpClient getProxiedClient(URL url, String s, int i)
        throws IOException {
        HttpTimeoutClient httpTimeoutClient = new HttpTimeoutClient(url, s, i);
        return httpTimeoutClient;
    }
}