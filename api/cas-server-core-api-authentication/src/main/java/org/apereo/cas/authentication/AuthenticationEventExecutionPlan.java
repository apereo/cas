package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.TenantAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.multitenancy.TenantExtractor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link AuthenticationEventExecutionPlan}.
 * A higher-level abstraction to encapsulate the registration of authentication handlers, etc. Each module would interact with
 * this interface that controls the registration and positioning of the handlers, etc.
 * The authentication manager contains this interface, and may dynamically for each transaction ask for a candidate list of handlers/resolvers, etc.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationEventExecutionPlan {

    /**
     * Default bean name.
     */
    String DEFAULT_BEAN_NAME = "authenticationEventExecutionPlan";

    /**
     * Register authentication handler.
     *
     * @param handler the handler
     * @return true/false
     */
    boolean registerAuthenticationHandler(AuthenticationHandler handler);

    /**
     * Register tenant authentication handler builder.
     *
     * @param handler the handler
     */
    void registerTenantAuthenticationHandlerBuilder(TenantAuthenticationHandlerBuilder handler);

    /**
     * Register authentication handlers.
     *
     * @param handlers the handlers
     */
    default void registerAuthenticationHandlers(final List<? extends AuthenticationHandler> handlers) {
        handlers.forEach(this::registerAuthenticationHandler);
    }

    /**
     * Register metadata populator.
     *
     * @param populator the populator
     */
    void registerAuthenticationMetadataPopulator(AuthenticationMetaDataPopulator populator);

    /**
     * Register authentication post processor.
     *
     * @param processor the populator
     */
    void registerAuthenticationPostProcessor(AuthenticationPostProcessor processor);

    /**
     * Register authentication pre processor.
     *
     * @param processor the populator
     */
    void registerAuthenticationPreProcessor(AuthenticationPreProcessor processor);

    /**
     * Register metadata populators.
     *
     * @param populator the populator
     */
    void registerAuthenticationMetadataPopulators(Collection<AuthenticationMetaDataPopulator> populator);

    /**
     * Register authentication policy.
     *
     * @param authenticationPolicy the authentication policy
     */
    void registerAuthenticationPolicy(AuthenticationPolicy authenticationPolicy);

    /**
     * Register authentication policies.
     *
     * @param authenticationPolicy the authentication policy
     */
    void registerAuthenticationPolicies(Collection<AuthenticationPolicy> authenticationPolicy);

    /**
     * Register authentication handler resolver.
     *
     * @param handlerResolver the handler resolver
     */
    void registerAuthenticationHandlerResolver(AuthenticationHandlerResolver handlerResolver);

    /**
     * Register authentication policy resolver.
     *
     * @param policyResolver the policy resolver
     */
    void registerAuthenticationPolicyResolver(AuthenticationPolicyResolver policyResolver);

    /**
     * Register authentication handler with principal resolver.
     *
     * @param plan the plan
     */
    void registerAuthenticationHandlerWithPrincipalResolver(Map<AuthenticationHandler, PrincipalResolver> plan);

    /**
     * Register authentication handler with principal resolver.
     *
     * @param handler           the handler
     * @param principalResolver the principal resolver
     * @return true if handler was able to successfully register itself, otherwise false.
     */
    boolean registerAuthenticationHandlerWithPrincipalResolver(AuthenticationHandler handler, PrincipalResolver principalResolver);

    /**
     * Register authentication handlers with principal resolver.
     *
     * @param handlers          the handlers
     * @param principalResolver the principal resolver
     */
    void registerAuthenticationHandlersWithPrincipalResolver(Collection<AuthenticationHandler> handlers, PrincipalResolver principalResolver);

    /**
     * Register authentication handler with principal resolvers.
     *
     * @param handlers          the handlers
     * @param principalResolver the principal resolver
     */
    void registerAuthenticationHandlersWithPrincipalResolver(List<AuthenticationHandler> handlers, List<PrincipalResolver> principalResolver);

    /**
     * Gets authentication handlers for transaction.
     *
     * @param transaction the transaction
     * @return the authentication handlers for transaction
     * @throws Throwable the throwable
     */
    Set<AuthenticationHandler> resolveAuthenticationHandlers(AuthenticationTransaction transaction) throws Throwable;

    /**
     * Gets authentication handlers.
     *
     * @return the authentication handlers
     */
    Set<AuthenticationHandler> resolveAuthenticationHandlers();

    /**
     * Gets authentication handlers.
     *
     * @return the authentication handlers
     */
    Set<AuthenticationHandler> getAuthenticationHandlers();
    
    /**
     * Gets tenant authentication handler builders.
     *
     * @return the tenant authentication handler builders
     */
    Collection<TenantAuthenticationHandlerBuilder> getTenantAuthenticationHandlerBuilders();

    /**
     * Gets authentication handlers by a filter.
     *
     * @param filter the filter
     * @return the authentication handlers by
     */
    default Set<AuthenticationHandler> resolveAuthenticationHandlersBy(final Predicate<AuthenticationHandler> filter) {
        return resolveAuthenticationHandlers().stream().filter(filter).collect(Collectors.toSet());
    }

    /**
     * Gets authentication metadata populators.
     *
     * @param transaction the transaction
     * @return the authentication metadata populators
     */
    Collection<AuthenticationMetaDataPopulator> getAuthenticationMetadataPopulators(AuthenticationTransaction transaction);

    /**
     * Gets authentication post processors.
     *
     * @param transaction the transaction
     * @return the authentication metadata populators
     */
    Collection<AuthenticationPostProcessor> getAuthenticationPostProcessors(AuthenticationTransaction transaction);

    /**
     * Gets authentication pre processors.
     *
     * @param transaction the transaction
     * @return the authentication metadata populators
     */
    Collection<AuthenticationPreProcessor> getAuthenticationPreProcessors(AuthenticationTransaction transaction);

    /**
     * Gets principal resolver for authentication transaction.
     *
     * @param handler     the handler
     * @param transaction the transaction
     * @return the principal resolver for authentication transaction
     */
    PrincipalResolver getPrincipalResolver(AuthenticationHandler handler, AuthenticationTransaction transaction);

    /**
     * Gets authentication policies.
     *
     * @param transaction the transaction
     * @return the authentication policies
     */
    Collection<AuthenticationPolicy> getAuthenticationPolicies(AuthenticationTransaction transaction);

    /**
     * Gets authentication policies.
     *
     * @param authentication the authentication
     * @return the authentication policies
     */
    Collection<AuthenticationPolicy> getAuthenticationPolicies(Authentication authentication);

    /**
     * Gets authentication policies.
     *
     * @return the authentication policies
     */
    Collection<AuthenticationPolicy> getAuthenticationPolicies();

    /**
     * Gets authentication handler resolvers.
     *
     * @param transaction the transaction
     * @return the authentication handler resolvers
     */
    Collection<AuthenticationHandlerResolver> getAuthenticationHandlerResolvers(AuthenticationTransaction transaction);

    /**
     * Gets authentication policy resolvers.
     *
     * @param transaction the transaction
     * @return the authentication handler resolvers
     */
    Collection<AuthenticationPolicyResolver> getAuthenticationPolicyResolvers(AuthenticationTransaction transaction);

    /**
     * Gets tenant extractor.
     *
     * @return the tenant extractor
     */
    TenantExtractor getTenantExtractor();
}
