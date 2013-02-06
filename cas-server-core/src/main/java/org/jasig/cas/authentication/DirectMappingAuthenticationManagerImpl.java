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

import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * Authentication Manager that provides a direct mapping between credentials
 * provided and the authentication handler used to authenticate the user.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class DirectMappingAuthenticationManagerImpl extends AbstractAuthenticationManager {

    @NotNull
    @Size(min=1)
    private Map<Class< ? extends Credentials>, DirectAuthenticationHandlerMappingHolder> credentialsMapping;

    /**
     * @throws IllegalArgumentException if a mapping cannot be found.
     * @see org.jasig.cas.authentication.AuthenticationManager#authenticate(org.jasig.cas.authentication.principal.Credentials)
     */
    @Override
    protected Pair<AuthenticationHandler, Principal> authenticateAndObtainPrincipal(final Credentials credentials) throws AuthenticationException {
        final Class< ? extends Credentials> credentialsClass = credentials.getClass();
        final DirectAuthenticationHandlerMappingHolder d = this.credentialsMapping.get(credentialsClass);

        Assert.notNull(d, "no mapping found for: " + credentialsClass.getName());

        final String handlerName = d.getAuthenticationHandler().getClass().getSimpleName();
        boolean authenticated = false;

        try {
            authenticated = d.getAuthenticationHandler().authenticate(credentials);
        } catch (final Exception e) {
            handleError(handlerName, credentials, e);
        }

        if (!authenticated) {
            log.info("{} failed to authenticate {}", handlerName, credentials);
            throw BadCredentialsAuthenticationException.ERROR;
        }
        log.info("{} successfully authenticated {}", handlerName, credentials);

        final Principal p = d.getCredentialsToPrincipalResolver().resolvePrincipal(credentials);

        return new Pair<AuthenticationHandler,Principal>(d.getAuthenticationHandler(), p);
    }

    public final void setCredentialsMapping(
        final Map<Class< ? extends Credentials>, DirectAuthenticationHandlerMappingHolder> credentialsMapping) {
        this.credentialsMapping = credentialsMapping;
    }
    
    public static final class DirectAuthenticationHandlerMappingHolder {

        private AuthenticationHandler authenticationHandler;

        private CredentialsToPrincipalResolver credentialsToPrincipalResolver;

        public DirectAuthenticationHandlerMappingHolder() {
            // nothing to do
        }

        public final AuthenticationHandler getAuthenticationHandler() {
            return this.authenticationHandler;
        }

        public void setAuthenticationHandler(
            final AuthenticationHandler authenticationHandler) {
            this.authenticationHandler = authenticationHandler;
        }

        public CredentialsToPrincipalResolver getCredentialsToPrincipalResolver() {
            return this.credentialsToPrincipalResolver;
        }

        public void setCredentialsToPrincipalResolver(
            final CredentialsToPrincipalResolver credentialsToPrincipalResolver) {
            this.credentialsToPrincipalResolver = credentialsToPrincipalResolver;
        }
    }

}
