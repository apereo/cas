/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.bind.support;

import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.bind.CredentialsBinder;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultSpringBindCredentialsBinder implements CredentialsBinder {

    /**
     * @see org.jasig.cas.web.bind.CredentialsBinder#bind(javax.servlet.http.HttpServletRequest, org.jasig.cas.authentication.principal.Credentials)
     */
    public void bind(HttpServletRequest request, Credentials credentials) {
        // don't do anything, Spring binding handles everything already due to the controller
    }

    /**
     * @see org.jasig.cas.web.bind.CredentialsBinder#supports(java.lang.Class)
     */
    public boolean supports(Class clazz) {
        return true; // supports any kind of credentials
    }
}