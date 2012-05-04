/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

/**
 * Default password encoder for the case where no password encoder is needed.
 * Encoding results in the same password that was passed in.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class PlainTextPasswordEncoder implements PasswordEncoder {

    public String encode(final String password) {
        return password;
    }
}
