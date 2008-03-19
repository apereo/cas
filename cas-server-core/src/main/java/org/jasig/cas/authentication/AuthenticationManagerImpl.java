/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.inspektr.audit.annotation.Auditable;
import org.inspektr.common.ioc.annotation.NotEmpty;
import org.inspektr.common.ioc.annotation.NotNull;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.NamedAuthenticationHandler;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;

/**
 * <p>
 * Default implementation of the AuthenticationManager. The
 * AuthenticationManager follows the following algorithm. The manager loops
 * through the array of AuthenticationHandlers searching for one that can
 * attempt to determine the validity of the credentials. If it finds one, it
 * tries that one. If that handler returns true, it continues on. If it returns
 * false, it looks for another handler. If it throws an exception, it aborts the
 * whole process and rethrows the exception. Next, it looks for a
 * CredentialsToPrincipalResolver that can handle the credentials in order to
 * create a Principal. Finally, it attempts to populate the Authentication
 * object's attributes map using AuthenticationAttributesPopulators
 * <p>
 * Behavior is determined by external beans attached through three configuration
 * properties. The Credentials are opaque to the manager. They are passed to the
 * external beans to see if any can process the actual type represented by the
 * Credentials marker.
 * <p>
 * AuthenticationManagerImpl requires the following properties to be set:
 * </p>
 * <ul>
 * <li> <code>authenticationHandlers</code> - The array of
 * AuthenticationHandlers that know how to process the credentials provided.
 * <li> <code>credentialsToPrincipalResolvers</code> - The array of
 * CredentialsToPrincipal resolvers that know how to process the credentials
 * provided.
 * </ul>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * @see org.jasig.cas.authentication.handler.AuthenticationHandler
 * @see org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver
 * @see org.jasig.cas.authentication.AuthenticationMetaDataPopulator
 */

public final class AuthenticationManagerImpl implements AuthenticationManager {

    /** Log instance for logging events, errors, warnings, etc. */
    private final Log log = LogFactory.getLog(AuthenticationManagerImpl.class);

    /** An array of authentication handlers. */
    @NotEmpty
    private List<AuthenticationHandler> authenticationHandlers;

    /** An array of CredentialsToPrincipalResolvers. */
    @NotEmpty
    private List<CredentialsToPrincipalResolver> credentialsToPrincipalResolvers;

    /** An array of AuthenticationAttributesPopulators. */
    @NotNull
    private List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators = new ArrayList<AuthenticationMetaDataPopulator>();

    @Auditable(action="AUTHENTICATION",successSuffix="_SUCCESS",failureSuffix="_FAILED",actionResolverClass=org.inspektr.audit.spi.support.ObjectCreationAuditableActionResolver.class,resourceResolverClass=org.jasig.cas.audit.spi.CredentialsAsFirstParameterResourceResolver.class)
    public Authentication authenticate(final Credentials credentials)
        throws AuthenticationException {
        boolean foundSupported = false;
        boolean authenticated = false;
        AuthenticationHandler authenticatedClass = null;
        
        for (final AuthenticationHandler authenticationHandler : this.authenticationHandlers) {
            if (authenticationHandler.supports(credentials)) {
                foundSupported = true;
                if (!authenticationHandler.authenticate(credentials)) {
                    if (log.isInfoEnabled()) {
                        log
                            .info("AuthenticationHandler: "
                                + authenticationHandler.getClass().getName()
                                + " failed to authenticate the user which provided the following credentials: "
                                + credentials.toString());
                    }
                } else {
                    if (log.isInfoEnabled()) {
                        log
                            .info("AuthenticationHandler: "
                                + authenticationHandler.getClass().getName()
                                + " successfully authenticated the user which provided the following credentials: "
                                + credentials.toString());
                    }
                    authenticatedClass = authenticationHandler;
                    authenticated = true;
                    break;
                }
            }
        }

        if (!authenticated) {
            if (foundSupported) {
                throw BadCredentialsAuthenticationException.ERROR;
            }

            throw UnsupportedCredentialsException.ERROR;
        }

        Authentication authentication = null;
        foundSupported = false;

        for (final CredentialsToPrincipalResolver credentialsToPrincipalResolver : this.credentialsToPrincipalResolvers) {
            if (credentialsToPrincipalResolver.supports(credentials)) {
                final Principal principal = credentialsToPrincipalResolver
                    .resolvePrincipal(credentials);
                foundSupported = true;
                if (principal != null) {
                    authentication = new MutableAuthentication(principal);
                    break;
                }
            }
        }

        if (authentication == null) {
            if (foundSupported) {
                if (log.isDebugEnabled()) {
                    log
                        .debug("CredentialsToPrincipalResolver found but no principal returned.");
                }

                throw BadCredentialsAuthenticationException.ERROR;
            }

            log.error("CredentialsToPrincipalResolver not found for "
                + credentials.getClass().getName());
            throw UnsupportedCredentialsException.ERROR;
        }
        
        if (authenticatedClass instanceof NamedAuthenticationHandler) {
            final NamedAuthenticationHandler a = (NamedAuthenticationHandler) authenticatedClass;
            authentication.getAttributes().put(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, a.getName());
        }        

        for (final AuthenticationMetaDataPopulator authenticationMetaDataPopulator : this.authenticationMetaDataPopulators) {
            authentication = authenticationMetaDataPopulator
                .populateAttributes(authentication, credentials);
        }

        return new ImmutableAuthentication(authentication.getPrincipal(),
            authentication.getAttributes());
    }

    /**
     * @param authenticationHandlers The authenticationHandlers to set.
     */
    public void setAuthenticationHandlers(
        final List<AuthenticationHandler> authenticationHandlers) {
        this.authenticationHandlers = authenticationHandlers;
    }

    /**
     * @param credentialsToPrincipalResolvers The
     * credentialsToPrincipalResolvers to set.
     */
    public void setCredentialsToPrincipalResolvers(
        final List<CredentialsToPrincipalResolver> credentialsToPrincipalResolvers) {
        this.credentialsToPrincipalResolvers = credentialsToPrincipalResolvers;
    }

    /**
     * @param authenticationMetaDataPopulators the
     * authenticationMetaDataPopulators to set.
     */
    public void setAuthenticationMetaDataPopulators(
        final List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators) {
        this.authenticationMetaDataPopulators = authenticationMetaDataPopulators;
    }
}
