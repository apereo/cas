/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.cas;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.beans.factory.InitializingBean;

import edu.yale.its.tp.cas.auth.PasswordHandler;

/**
 * Adaptor class to adapt the legacy CAS PasswordHandler to the new AuthenticationHandler
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class LegacyPasswordHandlerAdaptorAuthenticationHandler extends AbstractAuthenticationHandler implements InitializingBean {

    private PasswordHandler passwordHandler;

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.principal.Credentials)
     */
    public boolean authenticateInternal(final Credentials credentials) throws AuthenticationException {
        final LegacyCasCredentials casCredentials = (LegacyCasCredentials)credentials;

        return this.passwordHandler.authenticate(casCredentials.getServletRequest(), casCredentials.getUserName(), casCredentials.getPassword());
    }

    /**
     * @see org.jasig.cas.authentication.handler.support.AbstractAuthenticationHandler#supports(org.jasig.cas.authentication.principal.Credentials)
     */
    protected boolean supports(final Credentials credentials) {
        if (credentials == null)
            return false;

        return LegacyCasCredentials.class.equals(credentials.getClass());
    }

    /**
     * @param passwordHandler The passwordHandler to set.
     */
    public void setPasswordHandler(final PasswordHandler passwordHandler) {
        this.passwordHandler = passwordHandler;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.passwordHandler == null) {
            throw new IllegalStateException("passwordHandler must be set on " + this.getClass().getName());
        }
    }
}