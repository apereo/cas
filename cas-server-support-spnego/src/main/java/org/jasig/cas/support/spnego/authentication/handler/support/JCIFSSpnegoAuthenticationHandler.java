/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;

import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;

/**
 * Implementation of an AuthenticationHandler for SPNEGO supports. This Handler
 * support both NTLM and Kerberos. NTLM is disabled by default.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @since 3.1
 */
public final class JCIFSSpnegoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private Authentication authentication;

    /**
     * Principal contains the DomainName ? (true by default).
     */
    private boolean principalWithDomainName = true;

    /**
     * Allow SPNEGO/NTLM Token as valid credentials. (false by default)
     */
    private boolean isNTLMallowed = false;

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final SpnegoCredential spnegoCredential = (SpnegoCredential) credential;
        Principal principal;
        byte[] nextToken;
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
                spnegoCredential.setPrincipal(getSimplePrincipal(principal.getName(), true));
                success = this.isNTLMallowed;
            }
            // else => kerberos
            logger.debug("Kerberos Credential is valid for user [{}]", principal.getName());
            spnegoCredential.setPrincipal(getSimplePrincipal(principal.getName(), false));
            success = true;
        }

        if (!success) {
            throw new FailedLoginException("Principal is null, the processing of the SPNEGO Token failed");
        }
        return new HandlerResult(this, new BasicCredentialMetaData(credential), spnegoCredential.getPrincipal());
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

    protected SimplePrincipal getSimplePrincipal(final String name, final boolean isNtlm) {
        if (this.principalWithDomainName) {
            return new SimplePrincipal(name);
        }
        if (isNtlm) {
            return Pattern.matches("\\S+\\\\\\S+", name)
                    ? new SimplePrincipal(name.split("\\\\")[1])
                    : new SimplePrincipal(name);
        }
        return new SimplePrincipal(name.split("@")[0]);
    }
}
