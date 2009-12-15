/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.GoogleAccountsService;
import org.jasig.cas.authentication.principal.WebApplicationService;

/**
 * Constructs a GoogleAccounts compatible service and provides the public and
 * private keys.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class GoogleAccountsArgumentExtractor extends AbstractSingleSignOutEnabledArgumentExtractor {

    @NotNull
    private PublicKey publicKey;

    @NotNull
    private PrivateKey privateKey;

    private String alternateUsername;

    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return GoogleAccountsService.createServiceFrom(request,
            this.privateKey, this.publicKey, this.alternateUsername);
    }

    public void setPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Sets an alternate username to send to Google (i.e. fully qualified email address).  Relies on an appropriate
     * attribute available for the user.
     * <p>
     * Note that this is optional and the default is to use the normal identifier.
     *
     * @param alternateUsername the alternate username.  This is OPTIONAL.
     */
    public void setAlternateUsername(final String alternateUsername) {
        this.alternateUsername = alternateUsername;
    }
}
