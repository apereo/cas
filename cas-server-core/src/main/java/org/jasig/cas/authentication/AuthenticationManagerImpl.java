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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

public final class AuthenticationManagerImpl extends AbstractAuthenticationManager {

    /** An array of authentication handlers. */
    @NotNull
    @Size(min=1)
    private List<AuthenticationHandler> authenticationHandlers;

    /** An array of CredentialsToPrincipalResolvers. */
    @NotNull
    @Size(min=1)
    private List<CredentialsToPrincipalResolver> credentialsToPrincipalResolvers;

    @Override
    protected Pair<AuthenticationHandler, Principal> authenticateAndObtainPrincipal(final Credentials credentials)
            throws AuthenticationException {

        boolean foundSupported = false;
        final Map<String, Exception> errorMap = new LinkedHashMap<String, Exception>();
        final Map<String, HandlerResult> successMap = new LinkedHashMap<String, HandlerResult>();
        HandlerResult authenticatedResult = null;
        AuthenticationHandler authenticatedClass = null;
        String handlerName;

        for (final AuthenticationHandler authenticationHandler : this.authenticationHandlers) {
            if (authenticationHandler.supports(credentials)) {
                foundSupported = true;
                handlerName = authenticationHandler.getName();
                try {
                    authenticatedResult = authenticationHandler.authenticate(credentials);
                    successMap.put(handlerName, authenticatedResult);
                    log.info("{} successfully authenticated {}", handlerName, credentials);
                    authenticatedClass = authenticationHandler;
                    break;
                } catch (final Exception e) {
                    logAuthenticationHandlerError(handlerName, credentials, e);
                    errorMap.put(handlerName, e);
                }
            }
        }

        if (authenticatedResult == null) {
            if (errorMap.size() > 0) {
                throw new AuthenticationException(errorMap);
            }

            throw new AuthenticationException();
        }

        foundSupported = false;
        for (final CredentialsToPrincipalResolver credentialsToPrincipalResolver : this.credentialsToPrincipalResolvers) {
            if (credentialsToPrincipalResolver.supports(credentials)) {
                final Principal principal = credentialsToPrincipalResolver.resolvePrincipal(credentials);
                log.info("Resolved principal " + principal);
                foundSupported = true;
                if (principal != null) {
                    return new Pair<AuthenticationHandler,Principal>(authenticatedClass, principal);
                }
            }
        }

        if (foundSupported) {
            throw new PrincipalException("Resolver found but no principal returned.", errorMap, successMap);
        }

        throw new PrincipalException(
                "Resolver not found for " + credentials.getClass().getName(), errorMap, successMap);
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
}
