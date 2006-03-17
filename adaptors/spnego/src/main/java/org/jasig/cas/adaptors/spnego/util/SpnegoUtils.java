/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public final class SpnegoUtils {
    
    private static final Log LOG = LogFactory.getLog(SpnegoUtils.class);

    private SpnegoUtils() {
        // nothing to do here
    }

    public static final GSSContext getContext(final byte[] in) {
        if (in != null) {
            final GSSManager manager = GSSManager.getInstance();
            try {
                return manager.createContext(in);
            } catch (final GSSException gsse) {
                // Possibly manual unpacking of DER data and create context
                // using currently unsupported mechanisms.
            }
        }
        return null;
    }
    
    /**
     * Return a BASE64 encoded accept context.
     *
     * @param in Byte array retrieved from the BASE64-encoded part of the
     * HTTP Authorization-header.
     * @param context A context created from the byte array in data (the
     * "in"-parameter).
     * @return BASE64-encoded string to use in the HTTP WWW-Authenticate
     * header.
     * @throws GSSException If the context isn't acepted.
     */
    public static byte[] getToken(final byte[] token, final GSSContext context) {
        if (token != null && context != null) {
            try {
                final byte out[] = context.acceptSecContext(token, 0, token.length);
                return Base64.encodeBase64(out);
            } catch (final GSSException gsse) {
                // this should never happen so we'll log it as fatal and move on
                LOG.fatal(gsse.getMessage(), gsse);
            }
        }
        return null;
    }
}
