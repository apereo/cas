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

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.github.inspektr.audit.annotation.Audit;
import org.jasig.cas.authentication.support.AuthenticationMetaDataPopulator;
import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Base class for all {@link AuthenticationManager} componenents that ship with CAS. The general strategy for
 * authentication follows these steps:
 * <ul>
 *     <li>Iterate over all given credentials</li>
 *     <li>Iterate over all configured authentication handlers</li>
 *     <li>Attempt to authenticate a credential if a handler supports it</li>
 *     <li>On success attempt to resolve a principal</li>
 *     <ul>
 *         <li>Check whether a resolver is configured for the handler that authenticated the credential</li>
 *         <li>If a suitable resolver is found, attempt to resolve the principal</li>
 *         <li>If a suitable resolver is not found, use the principal resolved by the authentication handler</li>
 *     </ul>
 *     <li>Check whether the security policy (e.g. any, all) is satisfied</li>
 * </ul>
 *
 * It is an error condition to fail to resolve a principal or resolve multiple principals with distict IDs.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.3.5
 */
public abstract class AbstractAuthenticationManager implements AuthenticationManager {

    /** Log instance for logging events, errors, warnings, etc. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** An array of AuthenticationAttributesPopulators. */
    @NotNull
    private List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators = new ArrayList<AuthenticationMetaDataPopulator>();

    /** Map of authentication handlers to resolvers to be used when handler does not resolve a principal. */
    @NotNull
    private final Map<AuthenticationHandler, PrincipalResolver> handlerResolverMap;


    /**
     * Creates a new authentication manager with a list of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers List of authentication handlers.
     */
    protected AbstractAuthenticationManager(final List<AuthenticationHandler> handlers ) {
        Assert.notEmpty(handlers, "At least one authentication handler is required");
        this.handlerResolverMap = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>(handlers.size());
        for (final AuthenticationHandler handler : handlers) {
            this.handlerResolverMap.put(handler, null);
        }
    }

    /**
     * Creates a new authentication manager with a map of authentication handlers to the principal resolvers that
     * should be used upon successful authentication if no principal is resolved by the authentication handler. If
     * the order of evaluation of authentication handlers is important, a map that preserves insertion order
     * (e.g. {@link LinkedHashMap}) should be used.
     *
     * @param map A map of authentication handler to principal resolver.
     */
    protected AbstractAuthenticationManager(final Map<AuthenticationHandler, PrincipalResolver> map) {
        Assert.notEmpty(map, "At least one authentication handler is required");
        this.handlerResolverMap = map;
    }

    @Audit(
        action="AUTHENTICATION",
        actionResolverName="AUTHENTICATION_RESOLVER",
        resourceResolverName="AUTHENTICATION_RESOURCE_RESOLVER")
    @Profiled(tag = "AUTHENTICATE", logFailuresSeparately = false)
    public final Authentication authenticate(final Credential ... credentials)
            throws AuthenticationException, PrincipalException {

        Authentication authentication = authenticateInternal(credentials);
        final Principal principal = authentication.getPrincipal();
        if (principal  == null) {
            throw new UnresolvedPrincipalException();
        }
        for (final Principal p : authentication.getSuccesses().values()) {
            if (p != null && !principal.equals(p)) {
                throw new MixedPrincipalException(principal, p);
            }
        }
        for (final HandlerResult result : authentication.getSuccesses().keySet()) {
            authentication.getAttributes().put(AUTHENTICATION_METHOD_ATTRIBUTE, result.getHandlerName());
        }

        log.info("Authenticated {} with credentials {}.", principal, Arrays.asList(credentials));
        log.debug("Attribute map for {}: {}", principal.getId(), principal.getAttributes());

        for (final AuthenticationMetaDataPopulator populator : this.authenticationMetaDataPopulators) {
            for (final Credential credential : credentials) {
                authentication = populator.populateAttributes(authentication, credential);
            }
        }

        return new ImmutableAuthentication(authentication);
    }

    /**
     * @param authenticationMetaDataPopulators the authenticationMetaDataPopulators to set.
     */
    public final void setAuthenticationMetaDataPopulators(
            final List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators) {
        this.authenticationMetaDataPopulators = authenticationMetaDataPopulators;
    }

    /**
     * Follows the same contract as {@link AuthenticationManager#authenticate(Credential...)}.
     *
     * @param credentials One or more credentials to authenticate.
     *
     * @return An authentication containing a resolved principal and metadata about successful and failed
     * authentications. There SHOULD be a record of each attempted authentication, whether success or failure.
     *
     * @throws AuthenticationException When one or more credentials failed to validate such that security policy
     * was not satisfied.
     */
    protected final Authentication authenticateInternal(final Credential ... credentials)
            throws AuthenticationException {

        final MutableAuthentication authentication = new MutableAuthentication();
        final List<Credential> credentialList = Arrays.asList(credentials);
        final Map<Credential, HandlerResult> successes =
                new LinkedHashMap<Credential, HandlerResult>(credentials.length);
        final Map<Credential, HandlerError> failures =
                new LinkedHashMap<Credential, HandlerError>(credentials.length);
        boolean found;
        Principal principal;
        PrincipalResolver resolver;
        for (final Credential credential : credentials) {
            found = false;
            for (final AuthenticationHandler handler : this.handlerResolverMap.keySet()) {
                if (handler.supports(credential)) {
                    found = true;
                    try {
                        final HandlerResult result = handler.authenticate(credential);
                        log.info("{} successfully authenticated {}", handler.getName(), credential);
                        successes.put(credential, result);
                        resolver = this.handlerResolverMap.get(handler);
                        if (resolver == null) {
                            principal = result.getPrincipal();
                            log.debug(
                                    "No resolver configured for {}. " +
                                            "Falling back to the principal it resolved from {}: {}",
                                    handler.getName(),
                                    credential,
                                    principal);
                        } else {
                            principal = resolvePrincipal(handler.getName(), resolver, credential);
                        }
                        authentication.setPrincipal(principal);
                        authentication.getSuccesses().put(result, principal);
                        if (isSatisfied(credentialList, successes, failures, authentication)) {
                            return authentication;
                        }
                    } catch (final GeneralSecurityException e) {
                        log.info("{} failed authenticating {}", handler.getName(), credential);
                        failures.put(credential, new HandlerError(e, handler.getName()));
                        authentication.getFailures().put(handler.getName(), e);
                    } catch (final PreventedException e) {
                        failures.put(credential, new HandlerError(e, handler.getName()));
                        // Abort authentication immediately when authentication is prevented
                        throw new AuthenticationException(successes, failures);
                    }
                }
            }
            if (!found) {
                failures.put(credential, new HandlerError(new UnsupportedCredentialException(credential), null));
                log.warn(
                        "Cannot find authentication handler that supports {}, which suggests a configuration problem.",
                        credential);
            }
        }
        throw new AuthenticationException(successes, failures);
    }


    protected Principal resolvePrincipal(
            final String handlerName, final PrincipalResolver resolver, final Credential credential) {
        if (resolver.supports(credential)) {
            try {
                final Principal p = resolver.resolve(credential);
                log.debug("{} resolved {} from {}", resolver.getName(), p, credential);
                return p;
            } catch (final Exception e) {
                log.error("{} failed to resolve principal from {}", resolver.getName(), credential, e);
            }
        } else {
            log.warn(
                    "{} is configured to use {} but it does not support {}, which suggests configuration problem.",
                    handlerName,
                    resolver.getName(),
                    credential);
        }
        return null;
    }

    /**
     * Determines whether the given authentication satisfies the security policy imposed by the authentication
     * manager.
     *
     * @param credentials Collection of all provided credentials.
     * @param successes Map of information about successful credentials.
     * @param failures Map of information about failed credentials.
     * @param authentication Authentication to evaluate.
     *
     * @return True if authentication satisfies security policy, false otherwise.
     */
    protected abstract boolean isSatisfied(
            Collection<Credential> credentials,
            Map<Credential, HandlerResult> successes,
            Map<Credential, HandlerError> failures,
            Authentication authentication);

}
