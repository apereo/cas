/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.http;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Based on code by Niels Campbell.
 * http://tinyurl.com/dccnc
 * 
 * @author Niels Campbell (niels_campbell_at_lycos.co.uk)
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpTimeoutFactory implements URLStreamHandlerFactory {

    /** Timeout time. */
    private int iSoTimeout = 0;

    public HttpTimeoutFactory(final int iSoTimeout) {
        this.iSoTimeout = iSoTimeout;
    }

    public URLStreamHandler createURLStreamHandler(final String str) {
        return new HttpTimeoutHandler(this.iSoTimeout);
    }
}
