/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.support;

import org.jasig.cas.util.PasswordTranslator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id: PlainTextPasswordTranslatorTests.java,v 1.2 2005/02/27 05:49:26
 * sbattaglia Exp $
 */
public class PlainTextPasswordTranslatorTests extends TestCase {

    private final PasswordTranslator passwordTranslator = new PlainTextPasswordTranslator();

    public void testNullValueToTranslate() {
        assertEquals(null, this.passwordTranslator.translate(null));
    }

    public void testValueToTranslate() {
        assertEquals("CAS IS COOL", this.passwordTranslator
            .translate("CAS IS COOL"));
    }

}