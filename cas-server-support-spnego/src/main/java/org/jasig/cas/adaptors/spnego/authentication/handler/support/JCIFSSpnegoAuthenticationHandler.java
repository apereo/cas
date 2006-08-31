/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.handler.support;

import java.security.Principal;

import jcifs.spnego.Authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.adaptors.spnego.authentication.principal.SpnegoCredentials;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;

/**
 * Implementation of an AuthenticationHandler for SPNEGO supports. 
 * This Handler support both NTLM
 * and Kerberos. NTLM is disabled by default.
 * 
 * @author Arnaud Lessueur
 * @author Marc-Antoine Garrigue
 * @version $Id$
 * @since 3.1
 */
public final class JCIFSSpnegoAuthenticationHandler implements AuthenticationHandler {

    private Log logger = LogFactory.getLog(this.getClass());

    public Authentication authentication;

    /** Principal contains the DomainName ? (true by default). */
    public boolean principalWithDomainName = true;

    /** Allow SPNEGO/NTLM Token as valid credentials. (false by default) */
    public boolean isNTLMallowed = false;

    public boolean authenticate(final Credentials credentials) throws AuthenticationException {
        final SpnegoCredentials spnegoCredentials = (SpnegoCredentials) credentials;

        try {
            this.authentication.reset();
            this.authentication.process(spnegoCredentials.getInitToken());
            Principal principal = this.authentication.getPrincipal();
            if (this.authentication.getNextToken() != null) {
                logger.debug("Setting nextToken in credentials");
                spnegoCredentials.setNextToken(this.authentication.getNextToken());
            } else {
                logger.debug("nextToken is null");
            }
            if (principal != null) {
                if (spnegoCredentials.IsNtlm()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("NTLM Credentials is valid for user ["
                                + this.authentication.getPrincipal().getName() + "]");
                    }
                    spnegoCredentials.setPrincipal(getSimplePrincipal(this.authentication
                            .getPrincipal().getName(), true));
                    return isNTLMallowed();
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Kerberos Credentials is valid for user ["
                                + this.authentication.getPrincipal().getName() + "]");
                    }
                    spnegoCredentials.setPrincipal(getSimplePrincipal(this.authentication
                            .getPrincipal().getName(), false));
                    return true;
                }
            } else {
                logger.debug("Principal is null, the processing of the SPNEGO Token failed");
            }
        } catch (jcifs.spnego.AuthenticationException e) {
            throw new BadCredentialsAuthenticationException();
        }
        return false;
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null && SpnegoCredentials.class.equals(credentials.getClass());
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public boolean isPrincipalWithDomainName() {
        return principalWithDomainName;
    }

    public void setPrincipalWithDomainName(boolean principalWithDomainName) {
        this.principalWithDomainName = principalWithDomainName;
    }

    public boolean isNTLMallowed() {
        return isNTLMallowed;
    }

    public void setNTLMallowed(boolean isNTLMallowed) {
        this.isNTLMallowed = isNTLMallowed;
    }

    protected SimplePrincipal getSimplePrincipal(String name, boolean isNtlm) {
        if (isPrincipalWithDomainName()) {
            return new SimplePrincipal(name);
        } else {
            if (isNtlm) {
                return new SimplePrincipal(name.split("\\\\")[1]);
            } else {
                return new SimplePrincipal(name.split("@")[0]);
            }
        }
    }
}