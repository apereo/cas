package org.jasig.cas.authentication;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an authenticaiton manager that is inherently aware of multiple credentials and supports pluggable
 * security policy via the {@link AuthenticationPolicy} component. The authentication process is as follows:
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
 * It is an error condition to fail to resolve a principal.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("authenticationManager")
public class PolicyBasedAuthenticationManager implements AuthenticationManager {

    /** Log instance for logging events, errors, warnings, etc. */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** An array of AuthenticationAttributesPopulators. */
    @NotNull
    private List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators =
            new ArrayList<>();

    /** Authentication security policy. */
    @NotNull
    private AuthenticationPolicy authenticationPolicy = new AnyAuthenticationPolicy();

    /** Map of authentication handlers to resolvers to be used when handler does not resolve a principal. */
    @NotNull
    @Resource(name="authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> handlerResolverMap;


    /**
     * Instantiates a new Policy based authentication manager.
     */
    protected PolicyBasedAuthenticationManager() {}

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
    
    @Override
    @Audit(
        action="AUTHENTICATION",
        actionResolverName="AUTHENTICATION_RESOLVER",
        resourceResolverName="AUTHENTICATION_RESOURCE_RESOLVER")
    @Timed(name="AUTHENTICATE_TIMED")
    @Metered(name="AUTHENTICATE_METER")
    @Counted(name="AUTHENTICATE_COUNT", monotonic=true)
    public Authentication authenticate(final AuthenticationTransaction transaction) throws AuthenticationException {

        final AuthenticationBuilder builder = authenticateInternal(transaction.getCredentials());
        final Authentication authentication = builder.build();
        final Principal principal = authentication.getPrincipal();
        if (principal instanceof NullPrincipal) {
            throw new UnresolvedPrincipalException(authentication);
        }

        addAuthenticationMethodAttribute(builder, authentication);

        logger.info("Authenticated {} with credentials {}.", principal, transaction.getCredentials());
        logger.debug("Attribute map for {}: {}", principal.getId(), principal.getAttributes());

        populateAuthenticationMetadataAttributes(builder, transaction.getCredentials());

        return builder.build();
    }

    /**
     * Populate authentication metadata attributes.
     *
     * @param builder the builder
     * @param credentials the credentials
     */
    private void populateAuthenticationMetadataAttributes(final AuthenticationBuilder builder, final Collection<Credential> credentials) {
        for (final AuthenticationMetaDataPopulator populator : this.authenticationMetaDataPopulators) {
            for (final Credential credential : credentials) {
                if (populator.supports(credential)) {
                    populator.populateAttributes(builder, credential);
                }
            }
        }
    }

    /**
     * Add authentication method attribute.
     *
     * @param builder the builder
     * @param authentication the authentication
     */
    private void addAuthenticationMethodAttribute(final AuthenticationBuilder builder, final Authentication authentication) {
        for (final HandlerResult result : authentication.getSuccesses().values()) {
            builder.addAttribute(AUTHENTICATION_METHOD_ATTRIBUTE, result.getHandlerName());
        }
    }

    /**
     * Sets the authentication metadata populators that will be applied to every successful authentication event.
     *
     * @param populators Non-null list of metadata populators.
     */
    @Resource(name="authenticationMetadataPopulators")
    public final void setAuthenticationMetaDataPopulators(final List<AuthenticationMetaDataPopulator> populators) {
        this.authenticationMetaDataPopulators = populators;
    }

    /**
     * Sets the authentication policy used by this component.
     *
     * @param policy Non-null authentication policy. The default policy is {@link AnyAuthenticationPolicy}.
     */
    @Resource(name="authenticationPolicy")
    public void setAuthenticationPolicy(final AuthenticationPolicy policy) {
        this.authenticationPolicy = policy;
    }

    /**
     * Follows the same contract as {@link AuthenticationManager#authenticate(AuthenticationTransaction)}.
     *
     * @param credentials One or more credentials to authenticate.
     *
     * @return An authentication containing a resolved principal and metadata about successful and failed
     * authentications. There SHOULD be a record of each attempted authentication, whether success or failure.
     *
     * @throws AuthenticationException When one or more credentials failed authentication such that security policy
     * was not satisfied.
     */
    protected AuthenticationBuilder authenticateInternal(final Collection<Credential> credentials)
            throws AuthenticationException {

        final AuthenticationBuilder builder = new DefaultAuthenticationBuilder(NullPrincipal.getInstance());
        for (final Credential c : credentials) {
            builder.addCredential(new BasicCredentialMetaData(c));
        }
        boolean found;
        for (final Credential credential : credentials) {
            found = false;
            for (final Map.Entry<AuthenticationHandler, PrincipalResolver> entry : this.handlerResolverMap.entrySet()) {
                final AuthenticationHandler handler = entry.getKey();
                if (handler.supports(credential)) {
                    found = true;
                    try {
                        authenticateAndResolvePrincipal(builder, credential, entry.getValue(), handler);
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
                        "Cannot find authentication handler that supports [{}] of type [{}], which suggests a configuration problem.",
                        credential, credential.getClass().getSimpleName());
            }
        }
        evaluateProducedAuthenticationContext(builder);

        return builder;
    }

    /**
     * Evaluate produced authentication context.
     *
     * @param builder the builder
     * @throws AuthenticationException the authentication exception
     */
    private void evaluateProducedAuthenticationContext(final AuthenticationBuilder builder) throws AuthenticationException {
        // We apply an implicit security policy of at least one successful authentication
        if (builder.getSuccesses().isEmpty()) {
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
        // Apply the configured security policy
        if (!this.authenticationPolicy.isSatisfiedBy(builder.build())) {
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
    }

    /**
     * Authenticate and resolve principal.
     *
     * @param builder the builder
     * @param credential the credential
     * @param resolver the resolver
     * @param handler the handler
     * @throws GeneralSecurityException the general security exception
     * @throws PreventedException the prevented exception
     */
    private void authenticateAndResolvePrincipal(final AuthenticationBuilder builder, final Credential credential,
                                                 final PrincipalResolver resolver, final AuthenticationHandler handler)
            throws GeneralSecurityException, PreventedException {

        final Principal principal;
        final HandlerResult result = handler.authenticate(credential);
        builder.addSuccess(handler.getName(), result);
        logger.info("{} successfully authenticated {}", handler.getName(), credential);
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
