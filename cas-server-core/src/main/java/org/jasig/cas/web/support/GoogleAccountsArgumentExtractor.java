/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.GoogleAccountsService;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.util.annotation.NotNull;

/**
 * Constructs a GoogleAccounts compatible service and provides the public and
 * private keys.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public class GoogleAccountsArgumentExtractor extends AbstractArgumentExtractor {

    @NotNull
    private DSAPublicKey publicKey;

    @NotNull
    private DSAPrivateKey privateKey;

    public WebApplicationService extractService(final HttpServletRequest request) {
        return GoogleAccountsService.createServiceFrom(request,
            this.privateKey, this.publicKey);
    }

    public void setPrivateKey(final DSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(final DSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
