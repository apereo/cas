/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;

import org.inspektr.common.ioc.annotation.NotNull;
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
public class GoogleAccountsArgumentExtractor implements ArgumentExtractor {

    @NotNull
    private PublicKey publicKey;

    @NotNull
    private PrivateKey privateKey;

    public WebApplicationService extractService(final HttpServletRequest request) {
        return GoogleAccountsService.createServiceFrom(request,
            this.privateKey, this.publicKey);
    }

    public void setPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
