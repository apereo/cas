/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import org.inspektr.common.ioc.annotation.NotNull;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;

import java.util.Map;

/**
 * Ensures that all authentication handlers are tried, but if one is tried, the associated CredentialsToPrincipalResolver is used.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public class LinkedAuthenticationHandlerAndCredentialsToPrincipalResolverAuthenticationManager extends AbstractAuthenticationManager {

    @NotNull
    private final Map<AuthenticationHandler, CredentialsToPrincipalResolver> linkedHandlers;

    public LinkedAuthenticationHandlerAndCredentialsToPrincipalResolverAuthenticationManager(final Map<AuthenticationHandler,CredentialsToPrincipalResolver> linkedHandlers) {
        this.linkedHandlers = linkedHandlers; 
    }

    @Override
    protected Pair<AuthenticationHandler, Principal> authenticateAndObtainPrincipal(final Credentials credentials) throws AuthenticationException {
        boolean foundOneThatWorks = false;
        for (final AuthenticationHandler authenticationHandler : this.linkedHandlers.keySet()) {
            if (!authenticationHandler.supports(credentials)) {
                continue;
            }

            foundOneThatWorks = true;
            if (authenticationHandler.authenticate(credentials)) {
                final Principal p = this.linkedHandlers.get(authenticationHandler).resolvePrincipal(credentials);
                return new Pair<AuthenticationHandler,Principal>(authenticationHandler, p);
            }
        }

        if (foundOneThatWorks) {
            throw BadCredentialsAuthenticationException.ERROR;
        }

        throw UnsupportedCredentialsException.ERROR;
    }
}
