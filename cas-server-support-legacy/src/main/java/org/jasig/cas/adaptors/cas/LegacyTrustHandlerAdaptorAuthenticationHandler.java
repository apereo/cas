/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.cas;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import edu.yale.its.tp.cas.auth.TrustHandler;

/**
 * Adaptor class to adapt the legacy CAS TrustHandler to the new
 * AuthenticationHandler
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LegacyTrustHandlerAdaptorAuthenticationHandler implements
    AuthenticationHandler, InitializingBean {

    private TrustHandler trustHandler;

    public boolean authenticate(final Credentials credentials) {
        final LegacyCasTrustedCredentials casCredentials = (LegacyCasTrustedCredentials) credentials;

        return StringUtils.hasText(this.trustHandler.getUsername(casCredentials
            .getServletRequest()));
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && LegacyCasTrustedCredentials.class.equals(credentials.getClass());
    }

    /**
     * @param trustHandler The trustHandler to set.
     */
    public void setTrustHandler(final TrustHandler trustHandler) {
        this.trustHandler = trustHandler;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.trustHandler, "the trustHandler cannot be null");
    }
}