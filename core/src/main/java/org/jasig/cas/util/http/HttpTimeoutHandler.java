package org.jasig.cas.util.http;

import java.io.IOException;
import java.net.URL;

/**
 * Based on code from:
 * http://coding.derkeiler.com/Archive/Java/comp.lang.java.programmer/2004-01/3271.html
 */
public class HttpTimeoutHandler extends sun.net.www.protocol.http.Handler {

    private int iSoTimeout = 0;

    public HttpTimeoutHandler(int iSoTimeout) {
        // Divide the time out by two because two connection attempts are made in HttpClient.parseHTTP()

        if (iSoTimeout % 2 != 0) {
            iSoTimeout++;
        }
        this.iSoTimeout = (iSoTimeout / 2);
    }

    protected java.net.URLConnection openConnection(URL u) throws IOException {
        return new HttpTimeoutURLConnection(u, this, this.iSoTimeout);
    }

    protected String getProxy() {
        return this.proxy;
    }

    protected int getProxyPort() {
        return this.proxyPort;
    }
}