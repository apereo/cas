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
import java.util.HashSet;
import java.util.Set;
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

    /**
     * If not null allow only a set of realm.
     */
    private Set<String> allowedRealm;

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

        if (principal == null) {
            throw new FailedLoginException("Principal is null, the processing of the SPNEGO Token failed");
        }

        final String principalName = principal.getName();
        final boolean isNtlm = spnegoCredential.isNtlm();

        if (isNtlm){
            if (!this.isNTLMallowed){
                throw new FailedLoginException("NTLM not allowed");
            } else {
                logger.debug("NTLM Credential is valid for user [{}]", principalName);
            }
        } else {
            logger.debug("Kerberos Credential is valid for user [{}]", principalName);
        }

        final String[] idAndRealm = splitName(principalName, isNtlm);
        final String principalId = idAndRealm[0];
        final String realm = idAndRealm[1];

        if (allowedRealm != null && !allowedRealm.contains(realm==null ? null : realm.toUpperCase())) {
            throw new FailedLoginException("Realm ["+realm+"] is not allowed for principal ["+principalName+"]");
        }

        final SimplePrincipal simplePrincipal = new SimplePrincipal(
                    this.principalWithDomainName ? principalName : principalId
            );
        spnegoCredential.setPrincipal(simplePrincipal);

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

    public Set<String> getAllowedRealm() {
        return allowedRealm;
    }

    /**
     * Set upper case set of allowed realm.
     *
     * @param allowedRealm a set of allowed realm
     */
    public void setAllowedRealm(final Set<String> allowedRealm) {
        if (allowedRealm!=null){
            this.allowedRealm = new HashSet<>();
            for (String key : allowedRealm){
                this.allowedRealm.add(key.toUpperCase());
            }
        } else {
            this.allowedRealm = null;
        }
    }

    /**
     * Splits the principal name in id and realm.
     *
     * @param name the name
     * @param isNtlm the is ntlm
     * @return an array with the principal id in the position 0 and the realm in the position 1
     */
    protected String[] splitName(final String name, final boolean isNtlm){
        String id;
        String realm;
        if (isNtlm) {
            if (Pattern.matches("\\S+\\\\\\S+", name)) {
                final String[] split = name.split("\\\\");
                id = split[1];
                realm = split[0];
            } else {
                id = name;
                realm = null;
            }
        } else {
            if (Pattern.matches("\\S+@\\S+", name)) {
                final String[] split = name.split("@");
                id = split[0];
                realm = split[1];
            } else {
                id = name;
                realm = null;
            }

        }
        return new String[]{id, realm};
    }

}
