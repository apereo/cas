/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.spnego.authentication.handler.support;

import jcifs.spnego.Authentication;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.regex.Pattern;

/**
 * Implementation of an AuthenticationHandler for SPNEGO supports. This Handler
 * support both NTLM and Kerberos. NTLM is disabled by default.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @deprecated As of 4.1, the class name is abbreviated in a way that is not per camel-casing standards and will be renamed in the future.
 * @since 3.1
 */
@Deprecated
public final class JCIFSSpnegoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private Authentication authentication;

    /**
     * Principal contains the DomainName ? (true by default).
     */
    private boolean principalWithDomainName = true;

    /**
     * Allow SPNEGO/NTLM Token as valid credentials. (false by default)
     */
    private boolean isNTLMallowed;

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final SpnegoCredential spnegoCredential = (SpnegoCredential) credential;
        final java.security.Principal principal;
        final byte[] nextToken;
        if (!this.isNTLMallowed && spnegoCredential.isNtlm()) {
            throw new FailedLoginException("NTLM not allowed");
        }
        try {
            // proceed authentication using jcifs
            synchronized (this) {
                this.authentication.reset();
                this.authentication.process(spnegoCredential.getInitToken());
                principal = this.authentication.getPrincipal();
                nextToken = this.authentication.getNextToken();
            }
        } catch (final jcifs.spnego.AuthenticationException e) {
            throw new FailedLoginException(e.getMessage());
        }

        // evaluate jcifs response
        if (nextToken != null) {
            logger.debug("Setting nextToken in credential");
            spnegoCredential.setNextToken(nextToken);
        } else {
            logger.debug("nextToken is null");
        }

        boolean success = false;
        if (principal != null) {
            if (spnegoCredential.isNtlm()) {
                logger.debug("NTLM Credential is valid for user [{}]", principal.getName());
            } else {
                logger.debug("Kerberos Credential is valid for user [{}]", principal.getName());
            }
            spnegoCredential.setPrincipal(getPrincipal(principal.getName(), spnegoCredential.isNtlm()));
            success = true;
        }

        if (!success) {
            throw new FailedLoginException("Principal is null, the processing of the SPNEGO Token failed");
        }
        return new DefaultHandlerResult(this, new BasicCredentialMetaData(credential), spnegoCredential.getPrincipal());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof SpnegoCredential;
    }

    public void setAuthentication(final Authentication authentication) {
        this.authentication = authentication;
    }

    public void setPrincipalWithDomainName(final boolean principalWithDomainName) {
        this.principalWithDomainName = principalWithDomainName;
    }

    public void setNTLMallowed(final boolean isNTLMallowed) {
        this.isNTLMallowed = isNTLMallowed;
    }

    /**
     * @deprecated As of 4.1. Use {@link #getPrincipal(String, boolean)}
     * Gets the simple principal from the given name.
     *
     * @param name the name
     * @param isNtlm the is ntlm
     * @return the simple principal
     */
    @Deprecated
    protected SimplePrincipal getSimplePrincipal(final String name, final boolean isNtlm) {
        logger.warn("getSimplePrincipal() is deprecated and will be removed. Consider getPrincipal() instead.");

        if (this.principalWithDomainName) {
            return (SimplePrincipal) new DefaultPrincipalFactory().createPrincipal(name);
        }
        if (isNtlm) {
            return Pattern.matches("\\S+\\\\\\S+", name)
                    ? (SimplePrincipal) new DefaultPrincipalFactory().createPrincipal(name.split("\\\\")[1])
                    : (SimplePrincipal) new DefaultPrincipalFactory().createPrincipal(name);
        }
        return (SimplePrincipal) new DefaultPrincipalFactory().createPrincipal(name.split("@")[0]);
    }

    /**
     * Gets the principal from the given name. The principal
     * is created by the factory instance.
     *
     * @param name the name
     * @param isNtlm the is ntlm
     * @return the simple principal
     */
    protected Principal getPrincipal(final String name, final boolean isNtlm) {
        if (this.principalWithDomainName) {
            return this.principalFactory.createPrincipal(name);
        }
        if (isNtlm) {
            return Pattern.matches("\\S+\\\\\\S+", name)
                    ? this.principalFactory.createPrincipal(name.split("\\\\")[1])
                    : this.principalFactory.createPrincipal(name);
        }
        return this.principalFactory.createPrincipal(name.split("@")[0]);
    }
}
