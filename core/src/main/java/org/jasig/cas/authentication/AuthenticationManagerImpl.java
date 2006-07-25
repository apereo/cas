/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

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

public final class AuthenticationManagerImpl implements AuthenticationManager,
    InitializingBean {

    /** Log instance for logging events, errors, warnigs, etc. */
    private final Log log = LogFactory.getLog(AuthenticationManagerImpl.class);

    /** An array of authentication handlers. */
    private AuthenticationHandler[] authenticationHandlers;

    /** An array of CredentialsToPrincipalResolvers. */
    private CredentialsToPrincipalResolver[] credentialsToPrincipalResolvers;

    /** An array of AuthenticationAttributesPopulators. */
    private AuthenticationMetaDataPopulator[] authenticationMetaDataPopulators;

    public Authentication authenticate(final Credentials credentials)
        throws AuthenticationException {
        boolean foundSupported = false;
        boolean authenticated = false;

        for (int i = 0; i < this.authenticationHandlers.length; i++) {
            if (this.authenticationHandlers[i].supports(credentials)) {
                foundSupported = true;
                if (!this.authenticationHandlers[i].authenticate(credentials)) {
                    if (log.isInfoEnabled()) {
                        log
                            .info("AuthenticationHandler: "
                                + this.authenticationHandlers[i].getClass()
                                    .getName()
                                + " failed to authenticate the user which provided the following credentials: "
                                + credentials.toString());
                    }
                } else {
                    if (log.isInfoEnabled()) {
                        log
                            .info("AuthenticationHandler: "
                                + this.authenticationHandlers[i].getClass()
                                    .getName()
                                + " successfully authenticated the user which provided the following credentials: "
                                + credentials.toString());
                    }
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

        for (int i = 0; i < this.credentialsToPrincipalResolvers.length; i++) {
            if (this.credentialsToPrincipalResolvers[i].supports(credentials)) {
                final Principal principal = this.credentialsToPrincipalResolvers[i]
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

        for (int i = 0; i < this.authenticationMetaDataPopulators.length; i++) {
            authentication = this.authenticationMetaDataPopulators[i]
                .populateAttributes(authentication, credentials);
        }

        return new ImmutableAuthentication(authentication.getPrincipal(),
            authentication.getAttributes());
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(this.authenticationHandlers,
            "authenticationHandlers is a required property.");
        Assert.notEmpty(this.credentialsToPrincipalResolvers,
            "credentialsToPrincipalResolvers is a required property.");

        if (this.authenticationMetaDataPopulators == null) {
            this.authenticationMetaDataPopulators = new AuthenticationMetaDataPopulator[0];
        }
    }

    /**
     * @param authenticationHandlers The authenticationHandlers to set.
     */
    public void setAuthenticationHandlers(
        final AuthenticationHandler[] authenticationHandlers) {
        this.authenticationHandlers = authenticationHandlers;
    }

    /**
     * @param credentialsToPrincipalResolvers The
     * credentialsToPrincipalResolvers to set.
     */
    public void setCredentialsToPrincipalResolvers(
        final CredentialsToPrincipalResolver[] credentialsToPrincipalResolvers) {
        this.credentialsToPrincipalResolvers = credentialsToPrincipalResolvers;
    }

    /**
     * @param authenticationMetaDataPopulators the
     * authenticationMetaDataPopulators to set.
     */
    public void setAuthenticationMetaDataPopulators(
        final AuthenticationMetaDataPopulator[] authenticationMetaDataPopulators) {
        this.authenticationMetaDataPopulators = authenticationMetaDataPopulators;
    }
}
