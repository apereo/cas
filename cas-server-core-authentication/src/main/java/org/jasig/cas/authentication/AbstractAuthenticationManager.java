package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.inspektr.audit.annotation.Audit;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * This is {@link AbstractAuthenticationManager}, which provides common operations
 * around an authentication manager implementation.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractAuthenticationManager implements AuthenticationManager {
    /** Log instance for logging events, errors, warnings, etc. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** An array of AuthenticationAttributesPopulators. */
    @NotNull
    protected List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators =
            new ArrayList<>();

    /** Map of authentication handlers to resolvers to be used when handler does not resolve a principal. */
    @NotNull
    @Resource(name="authenticationHandlersResolvers")
    protected Map<AuthenticationHandler, PrincipalResolver> handlerResolverMap;

    /**
     * The Authentication handler resolver.
     */
    @NotNull
    @Resource(name="registeredServiceAuthenticationHandlerResolver")
    protected AuthenticationHandlerResolver authenticationHandlerResolver =
            new RegisteredServiceAuthenticationHandlerResolver();

    /**
     * Instantiates a new Policy based authentication manager.
     */
    protected AbstractAuthenticationManager() {}

    /**
     * Creates a new authentication manager with a varargs array of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers One or more authentication handlers.
     */
    protected AbstractAuthenticationManager(final AuthenticationHandler ... handlers) {
        this(Arrays.asList(handlers));
    }

    /**
     * Creates a new authentication manager with a list of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers Non-null list of authentication handlers containing at least one entry.
     */
    protected AbstractAuthenticationManager(final List<AuthenticationHandler> handlers) {
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
    protected AbstractAuthenticationManager(final Map<AuthenticationHandler, PrincipalResolver> map) {
        Assert.notEmpty(map, "At least one authentication handler is required");
        this.handlerResolverMap = map;
    }

    /**
     * Populate authentication metadata attributes.
     *
     * @param builder the builder
     * @param credentials the credentials
     */
    protected void populateAuthenticationMetadataAttributes(final AuthenticationBuilder builder, final Collection<Credential> credentials) {
        for (final AuthenticationMetaDataPopulator populator : this.authenticationMetaDataPopulators) {
            credentials.stream().filter(populator::supports).forEach(credential -> populator.populateAttributes(builder, credential));
        }
    }

    /**
     * Add authentication method attribute.
     *
     * @param builder the builder
     * @param authentication the authentication
     */
    protected void addAuthenticationMethodAttribute(final AuthenticationBuilder builder, final Authentication authentication) {
        for (final HandlerResult result : authentication.getSuccesses().values()) {
            builder.addAttribute(AUTHENTICATION_METHOD_ATTRIBUTE, result.getHandlerName());
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

    @Override
    @Audit(
            action="AUTHENTICATION",
            actionResolverName="AUTHENTICATION_RESOLVER",
            resourceResolverName="AUTHENTICATION_RESOURCE_RESOLVER")
    @Timed(name="AUTHENTICATE_TIMER")
    @Metered(name="AUTHENTICATE_METER")
    @Counted(name="AUTHENTICATE_COUNT", monotonic=true)
    public Authentication authenticate(final AuthenticationTransaction transaction) throws AuthenticationException {
        final AuthenticationBuilder builder = authenticateInternal(transaction);
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
     * Authenticate and resolve principal.
     *
     * @param builder the builder
     * @param credential the credential
     * @param resolver the resolver
     * @param handler the handler
     * @throws GeneralSecurityException the general security exception
     * @throws PreventedException the prevented exception
     */
    protected void authenticateAndResolvePrincipal(final AuthenticationBuilder builder, final Credential credential,
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
        if (principal != null) {
            builder.setPrincipal(principal);
        }
    }

    /**
     * Follows the same contract as {@link AuthenticationManager#authenticate(AuthenticationTransaction)}.
     *
     * @param transaction the authentication transaction
     * @return An authentication containing a resolved principal and metadata about successful and failed authentications.
     * There SHOULD be a record of each attempted authentication, whether success or failure.
     * @throws AuthenticationException When one or more credentials failed authentication such that security policy was not satisfied.
     */
    protected abstract AuthenticationBuilder authenticateInternal(final AuthenticationTransaction transaction)
            throws AuthenticationException;

    /**
     * Sets the authentication metadata populators that will be applied to every successful authentication event.
     *
     * @param populators Non-null list of metadata populators.
     */
    @Resource(name="authenticationMetadataPopulators")
    public final void setAuthenticationMetaDataPopulators(final List<AuthenticationMetaDataPopulator> populators) {
        this.authenticationMetaDataPopulators = populators;
    }

    public void setAuthenticationHandlerResolver(final AuthenticationHandlerResolver authenticationHandlerResolver) {
        this.authenticationHandlerResolver = authenticationHandlerResolver;
    }
}
