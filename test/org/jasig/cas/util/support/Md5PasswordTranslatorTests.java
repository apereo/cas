/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util.support;

import org.jasig.cas.util.PasswordTranslator;

import junit.framework.TestCase;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class Md5PasswordTranslatorTests extends TestCase {
    private PasswordTranslator passwordTranslator = new Md5PasswordTranslator();
    
    public void testNullPassword() {
        assertEquals(null, passwordTranslator.translate(null));
    }
    
    public void testHash() {
        assertEquals("1f3870be274f6c49b3e31a0c6728957f", passwordTranslator.translate("apple"));
    }
}
