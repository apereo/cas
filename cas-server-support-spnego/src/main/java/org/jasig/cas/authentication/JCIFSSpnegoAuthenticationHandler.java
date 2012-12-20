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
package org.jasig.cas.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;

import jcifs.spnego.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an AuthenticationHandler for SPNEGO supports. This Handler
 * support both NTLM and Kerberos. NTLM is disabled by default.
 * 
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class JCIFSSpnegoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Authentication authentication;

    /**
     * Principal contains the DomainName ? (true by default).
     */
    private boolean principalWithDomainName = true;

    /**
     * Allow SPNEGO/NTLM Token as valid credentials. (false by default)
     */
    private boolean isNTLMallowed = false;

    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, IOException {
        final SpnegoCredential spnegoCredentials = (SpnegoCredential) credential;
        final Principal principal;
        final byte[] nextToken;
        try {
            // proceed authentication using jcifs
            synchronized (this) {
                this.authentication.reset();
                this.authentication.process(spnegoCredentials.getInitToken());
                principal = this.authentication.getPrincipal();
                nextToken = this.authentication.getNextToken();
            }
        } catch (jcifs.spnego.AuthenticationException e) {
            log.debug("Authentication failed due to error {}", e);
            throw new FailedLoginException();
        }
        // evaluate jcifs response
        if (nextToken != null) {
            logger.debug("Setting nextToken in credential");
            spnegoCredentials.setNextToken(nextToken);
        } else {
            logger.debug("nextToken is null");
        }

        if (principal != null) {
            if (spnegoCredentials.isNtlm()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("NTLM Credential is valid for user ["
                        + principal.getName() + "]");
                }
                spnegoCredentials.setPrincipal(getSimplePrincipal(principal.getName(), true));
                if (!this.isNTLMallowed) {
                    throw new FailedLoginException("NTLM not allowed.");
                }
            } else {
                logger.debug("Kerberos Credential is valid for user {}", principal.getName());
                spnegoCredentials.setPrincipal(getSimplePrincipal(principal.getName(), false));
            }
            return new HandlerResult(this, new SimplePrincipal(principal.getName()));
        } else {
            logger.debug("Principal is null, the processing of the SPNEGO Token failed");
        }
        throw new FailedLoginException();
    }

    public boolean supports(final Credential credential) {
        return credential != null
            && SpnegoCredential.class.equals(credential.getClass());
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

    protected SimplePrincipal getSimplePrincipal(final String name,
        final boolean isNtlm) {
        if (this.principalWithDomainName) {
            return new SimplePrincipal(name);
        }
        if (isNtlm) {
        	return Pattern.matches("\\S+\\\\\\S+", name) ? new SimplePrincipal
        			(name.split("\\\\")[1]) : new SimplePrincipal(name);
        }
        return new SimplePrincipal(name.split("@")[0]);
    }
}
