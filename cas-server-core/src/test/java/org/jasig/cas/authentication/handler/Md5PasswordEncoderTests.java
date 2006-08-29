/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Md5PasswordEncoderTests extends TestCase {

    private final PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

    public void testNullPassword() {
        assertEquals(null, this.passwordEncoder.encode(null));
    }

    public void testHash() {
        assertEquals("1f3870be274f6c49b3e31a0c6728957f", this.passwordEncoder
            .encode("apple"));
    }
}
