package org.jasig.cas.util.http;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Based on code from:
 * http://coding.derkeiler.com/Archive/Java/comp.lang.java.programmer/2004-01/3271.html
 */

public class HttpTimeoutFactory implements URLStreamHandlerFactory {

    private int iSoTimeout = 0;

    public HttpTimeoutFactory(int iSoTimeout) {
        this.iSoTimeout = iSoTimeout;
    }

    public URLStreamHandler createURLStreamHandler(String str) {
        return new HttpTimeoutHandler(this.iSoTimeout);
    }
}