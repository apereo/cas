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
package org.jasig.cas.authentication;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.jasig.inspektr.audit.annotation.Audit;
import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an authenticaiton manager that is inherently aware of multiple credentials and supports pluggable
 * security policy via the {@link AuthenticationPolicy} component. The authentication process is as follows:
 *
 * <ul>
 *   <li>For each given credential do the following:
 *     <ul>
 *       <li>Iterate over all configured authentication handlers.</li>
 *       <li>Attempt to authenticate a credential if a handler supports it.</li>
 *       <li>On success attempt to resolve a principal by doing the following:
 *         <ul>
 *           <li>Check whether a resolver is configured for the handler that authenticated the credential.</li>
 *           <li>If a suitable resolver is found, attempt to resolve the principal.</li>
 *           <li>If a suitable resolver is not found, use the principal resolved by the authentication handler.</li>
 *         </ul>
 *       </li>
 *       <li>Check whether the security policy (e.g. any, all) is satisfied.
 *         <ul>
 *           <li>If security policy is met return immediately.</li>
 *           <li>Continue if security policy is not met.</li>
 *         </ul>
 *       </li>
 *     </ul>
 *   </li>
 *   <li>
 *     After all credentials have been attempted check security policy again.
 *     Note there is an implicit security policy that requires at least one credential to be authenticated.
 *     Then the security policy given by {@link #setAuthenticationPolicy(AuthenticationPolicy)} is applied.
 *     In all cases {@link AuthenticationException} is raised if security policy is not met.
 *   </li>
 * </ul>
 *
 * It is an error condition to fail to resolve a principal.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PolicyBasedAuthenticationManager implements AuthenticationManager {

    /** Log instance for logging events, errors, warnings, etc. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** An array of AuthenticationAttributesPopulators. */
    @NotNull
    private List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators =
            new ArrayList<>();

    /** Authentication security policy. */
    @NotNull
    private AuthenticationPolicy authenticationPolicy = new AnyAuthenticationPolicy();

    /** Map of authentication handlers to resolvers to be used when handler does not resolve a principal. */
    @NotNull
    private final Map<AuthenticationHandler, PrincipalResolver> handlerResolverMap;


    /**
     * Creates a new authentication manager with a varargs array of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers One or more authentication handlers.
     */
    public PolicyBasedAuthenticationManager(final AuthenticationHandler ... handlers) {
        this(Arrays.asList(handlers));
    }

    /**
     * Creates a new authentication manager with a list of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers Non-null list of authentication handlers containing at least one entry.
     */
    public PolicyBasedAuthenticationManager(final List<AuthenticationHandler> handlers) {
        Assert.notEmpty(handlers, "At least one authentication handler is required");
        this.handlerResolverMap = new LinkedHashMap<>(
                handlers.size());
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
     * @param map Non-null map of authentication handler to principal resolver containing at least one entry.
     */
    public PolicyBasedAuthenticationManager(final Map<AuthenticationHandler, PrincipalResolver> map) {
        Assert.notEmpty(map, "At least one authentication handler is required");
        this.handlerResolverMap = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Audit(
        action="AUTHENTICATION",
        actionResolverName="AUTHENTICATION_RESOLVER",
        resourceResolverName="AUTHENTICATION_RESOURCE_RESOLVER")
    @Timed(name="AUTHENTICATE")
    @Metered(name="AUTHENTICATE")
    @Counted(name="AUTHENTICATE", monotonic=true)
    public final Authentication authenticate(final Credential... credentials) throws AuthenticationException {

        final AuthenticationBuilder builder = authenticateInternal(credentials);
        final Authentication authentication = builder.build();
        final Principal principal = authentication.getPrincipal();
        if (principal  instanceof NullPrincipal) {
            throw new UnresolvedPrincipalException(authentication);
        }

        for (final HandlerResult result : authentication.getSuccesses().values()) {
            builder.addAttribute(AUTHENTICATION_METHOD_ATTRIBUTE, result.getHandlerName());
        }

        logger.info("Authenticated {} with credentials {}.", principal, Arrays.asList(credentials));
        logger.debug("Attribute map for {}: {}", principal.getId(), principal.getAttributes());

        for (final AuthenticationMetaDataPopulator populator : this.authenticationMetaDataPopulators) {
            for (final Credential credential : credentials) {
                if (populator.supports(credential)) {
                    populator.populateAttributes(builder, credential);
                }
            }
        }

        return builder.build();
    }

    /**
     * Sets the authentication metadata populators that will be applied to every successful authentication event.
     *
     * @param populators Non-null list of metadata populators.
     */
    public final void setAuthenticationMetaDataPopulators(final List<AuthenticationMetaDataPopulator> populators) {
        this.authenticationMetaDataPopulators = populators;
    }

    /**
     * Sets the authentication policy used by this component.
     *
     * @param policy Non-null authentication policy. The default policy is {@link AnyAuthenticationPolicy}.
     */
    public void setAuthenticationPolicy(final AuthenticationPolicy policy) {
        this.authenticationPolicy = policy;
    }

    /**
     * Follows the same contract as {@link AuthenticationManager#authenticate(Credential...)}.
     *
     * @param credentials One or more credentials to authenticate.
     *
     * @return An authentication containing a resolved principal and metadata about successful and failed
     * authentications. There SHOULD be a record of each attempted authentication, whether success or failure.
     *
     * @throws AuthenticationException When one or more credentials failed authentication such that security policy
     * was not satisfied.
     */
    protected AuthenticationBuilder authenticateInternal(final Credential... credentials)
            throws AuthenticationException {

        final AuthenticationBuilder builder = new DefaultAuthenticationBuilder(NullPrincipal.getInstance());
        for (final Credential c : credentials) {
            builder.addCredential(new BasicCredentialMetaData(c));
        }
        boolean found;
        Principal principal;
        PrincipalResolver resolver;
        for (final Credential credential : credentials) {
            found = false;
            for (final Map.Entry<AuthenticationHandler, PrincipalResolver> entry : this.handlerResolverMap.entrySet()) {
                final AuthenticationHandler handler = entry.getKey();
                if (handler.supports(credential)) {
                    found = true;
                    try {
                        final HandlerResult result = handler.authenticate(credential);
                        builder.addSuccess(handler.getName(), result);
                        logger.info("{} successfully authenticated {}", handler.getName(), credential);
                        resolver = entry.getValue();
                        if (resolver == null) {
                            principal = result.getPrincipal();
                            logger.debug(
                                    "No resolver configured for {}. Falling back to handler principal {}",
                                    handler.getName(),
                                    principal);
                        } else {
                            principal = resolvePrincipal(handler.getName(), resolver, credential);
                        }
                        // Must avoid null principal since AuthenticationBuilder/ImmutableAuthentication
                        // require principal to be non-null
                        if (principal != null) {
                            builder.setPrincipal(principal);
                        }
                        if (this.authenticationPolicy.isSatisfiedBy(builder.build())) {
                            return builder;
                        }
                    } catch (final GeneralSecurityException e) {
                        logger.info("{} failed authenticating {}", handler.getName(), credential);
                        logger.debug("{} exception details: {}", handler.getName(), e.getMessage());
                        builder.addFailure(handler.getName(), e.getClass());
                    } catch (final PreventedException e) {
                        logger.error("{}: {}  (Details: {})", handler.getName(), e.getMessage(), e.getCause().getMessage());
                        builder.addFailure(handler.getName(), e.getClass());
                    }
                }
            }
            if (!found) {
                logger.warn(
                        "Cannot find authentication handler that supports {}, which suggests a configuration problem.",
                        credential);
            }
        }
        // We apply an implicit security policy of at least one successful authentication
        if (builder.getSuccesses().isEmpty()) {
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
        // Apply the configured security policy
        if (!this.authenticationPolicy.isSatisfiedBy(builder.build())) {
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
        return builder;
    }

      
    /**
     * Resolve principal.
     *
     * @param handlerName the handler name
     * @param resolver the resolver
     * @param credential the credential
     * @return the principal
     */
    protected Principal resolvePrincipal(
            final String handlerName, final PrincipalResolver resolver, final Credential credential) {
        if (resolver.supports(credential)) {
            try {
                final Principal p = resolver.resolve(credential);
                logger.debug("{} resolved {} from {}", resolver, p, credential);
                return p;
            } catch (final Exception e) {
                logger.error("{} failed to resolve principal from {}", resolver, credential, e);
            }
        } else {
            logger.warn(
                    "{} is configured to use {} but it does not support {}, which suggests a configuration problem.",
                    handlerName,
                    resolver,
                    credential);
        }
        return null;
    }

}
