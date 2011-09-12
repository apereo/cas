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
public final class DefaultPasswordEncoderTests extends TestCase {
    
    private final PasswordEncoder passwordEncoder = new DefaultPasswordEncoder("MD5");

    public void testNullPassword() {
        assertEquals(null, this.passwordEncoder.encode(null));
    }

    public void testMd5Hash() {
        assertEquals("1f3870be274f6c49b3e31a0c6728957f", this.passwordEncoder
            .encode("apple"));
    }
    
    public void testSha1Hash() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("SHA1");
        
        final String hash = pe.encode("this is a test");
        
        assertEquals("fa26be19de6bff93f70bc2308434e4a440bbad02", hash);
        
    }
    
    public void testSha1Hash2() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("SHA1");
        
        final String hash = pe.encode("TEST of the SYSTEM");
        
        assertEquals("82ae28dfad565dd9882b94498a271caa29025d5f", hash);
        
    }
    
    public void testInvalidEncodingType() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("scott");
        try {
            pe.encode("test");
            fail("exception expected.");
        } catch (final Exception e) {
            return;
        }
    }
}
