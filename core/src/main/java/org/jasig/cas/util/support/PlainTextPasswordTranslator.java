/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util.support;

import org.jasig.cas.util.PasswordTranslator;

/**
 * Default password translator for the case where no password translator is
 * needed. Translation results in the same password that was passed in.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class PlainTextPasswordTranslator implements PasswordTranslator {

    public String translate(final String password) {
        return password;
    }
}
