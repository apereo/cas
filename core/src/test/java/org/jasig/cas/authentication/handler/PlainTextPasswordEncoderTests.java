/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class PlainTextPasswordEncoderTests extends TestCase {

    private final PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    public void testNullValueToTranslate() {
        assertEquals(null, this.passwordEncoder.encode(null));
    }

    public void testValueToTranslate() {
        assertEquals("CAS IS COOL", this.passwordEncoder.encode("CAS IS COOL"));
    }

}