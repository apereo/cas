package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is {@link DefaultAuthenticationEventExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DefaultAuthenticationEventExecutionPlan implements AuthenticationEventExecutionPlan {
    private static final int MAP_SIZE = 8;

    private final List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulatorList = new ArrayList<>(0);

    private final List<AuthenticationPostProcessor> authenticationPostProcessors = new ArrayList<>(0);

    private final List<AuthenticationPreProcessor> authenticationPreProcessors = new ArrayList<>(0);

    private final List<AuthenticationPolicy> authenticationPolicies = new ArrayList<>(0);

    private final List<AuthenticationHandlerResolver> authenticationHandlerResolvers = new ArrayList<>(0);

    private final List<AuthenticationPolicyResolver> authenticationPolicyResolvers = new ArrayList<>(0);

    private final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlerPrincipalResolverMap = new LinkedHashMap<>(MAP_SIZE);

    @Override
    public void registerAuthenticationHandler(final AuthenticationHandler handler) {
        registerAuthenticationHandlerWithPrincipalResolver(handler, null);
    }

    @Override
    public void registerAuthenticationMetadataPopulator(final AuthenticationMetaDataPopulator populator) {
        LOGGER.trace("Registering metadata populator [{}] into the execution plan", populator);
        authenticationMetaDataPopulatorList.add(populator);
    }

    @Override
    public void registerAuthenticationPostProcessor(final AuthenticationPostProcessor processor) {
        LOGGER.debug("Registering authentication post processor [{}] into the execution plan", processor);
        authenticationPostProcessors.add(processor);
    }

    @Override
    public void registerAuthenticationPreProcessor(final AuthenticationPreProcessor processor) {
        LOGGER.debug("Registering authentication pre processor [{}] into the execution plan", processor);
        authenticationPreProcessors.add(processor);
    }

    @Override
    public void registerAuthenticationMetadataPopulators(final Collection<AuthenticationMetaDataPopulator> populators) {
        populators.forEach(this::registerAuthenticationMetadataPopulator);
    }

    @Override
    public void registerAuthenticationPolicy(final AuthenticationPolicy authenticationPolicy) {
        this.authenticationPolicies.add(authenticationPolicy);
    }

    @Override
    public void registerAuthenticationPolicies(final Collection<AuthenticationPolicy> authenticationPolicy) {
        this.authenticationPolicies.addAll(authenticationPolicy);
    }

    @Override
    public void registerAuthenticationHandlerResolver(final AuthenticationHandlerResolver handlerResolver) {
        this.authenticationHandlerResolvers.add(handlerResolver);
    }

    @Override
    public void registerAuthenticationPolicyResolver(final AuthenticationPolicyResolver policyResolver) {
        this.authenticationPolicyResolvers.add(policyResolver);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final Map<AuthenticationHandler, PrincipalResolver> plan) {
        plan.forEach(this::registerAuthenticationHandlerWithPrincipalResolver);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final AuthenticationHandler handler, final PrincipalResolver principalResolver) {
        if (principalResolver == null) {
            LOGGER.trace("Registering handler [{}] with no principal resolver into the execution plan", handler.getName());
        } else {
            LOGGER.trace("Registering handler [{}] principal resolver [{}] into the execution plan", handler.getName(), principalResolver.getName());
        }
        this.authenticationHandlerPrincipalResolverMap.put(handler, principalResolver);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolvers(final Collection<AuthenticationHandler> handlers,
                                                                    final PrincipalResolver principalResolver) {
        handlers.forEach(h -> registerAuthenticationHandlerWithPrincipalResolver(h, principalResolver));
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolvers(final List<AuthenticationHandler> handlers, final List<PrincipalResolver> principalResolver) {
        if (handlers.size() != principalResolver.size()) {
            LOGGER.error("Total number of authentication handlers must match the number of provided principal resolvers");
            return;
        }
        IntStream.range(0, handlers.size())
            .forEach(i -> registerAuthenticationHandlerWithPrincipalResolver(handlers.get(i), principalResolver.get(i)));
    }

    @Override
    public @NonNull Set<AuthenticationHandler> getAuthenticationHandlers(final AuthenticationTransaction transaction) {
        val handlers = getAuthenticationHandlers();
        LOGGER.debug("Candidate/Registered authentication handlers for this transaction are [{}]", handlers);
        val handlerResolvers = getAuthenticationHandlerResolvers(transaction);
        LOGGER.debug("Authentication handler resolvers for this transaction are [{}]", handlerResolvers);

        val resolvedHandlers = handlerResolvers.stream()
            .filter(r -> r.supports(handlers, transaction))
            .map(r -> r.resolve(handlers, transaction))
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (resolvedHandlers.isEmpty()) {
            LOGGER.debug("Authentication handler resolvers produced no candidate authentication handler. Using the default handler resolver instead...");
            val defaultHandlerResolver = new DefaultAuthenticationHandlerResolver();
            if (defaultHandlerResolver.supports(handlers, transaction)) {
                resolvedHandlers.addAll(defaultHandlerResolver.resolve(handlers, transaction));
            }
        }

        if (resolvedHandlers.isEmpty()) {
            throw new AuthenticationException("No authentication handlers could be resolved to support the authentication transaction");
        }
        LOGGER.debug("Resolved and finalized authentication handlers to carry out this authentication transaction are [{}]", handlerResolvers);
        return resolvedHandlers;
    }

    @Override
    public Set<AuthenticationHandler> getAuthenticationHandlers() {
        val handlers = authenticationHandlerPrincipalResolverMap.keySet().toArray(AuthenticationHandler[]::new);
        AnnotationAwareOrderComparator.sortIfNecessary(handlers);
        return new LinkedHashSet<>(CollectionUtils.wrapList(handlers));
    }

    @Override
    public Collection<AuthenticationMetaDataPopulator> getAuthenticationMetadataPopulators(final AuthenticationTransaction transaction) {
        val list = new ArrayList<>(this.authenticationMetaDataPopulatorList);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.debug("Sorted and registered metadata populators for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationPostProcessor> getAuthenticationPostProcessors(final AuthenticationTransaction transaction) {
        val list = new ArrayList<>(this.authenticationPostProcessors);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication post processors for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationPreProcessor> getAuthenticationPreProcessors(final AuthenticationTransaction transaction) {
        val list = new ArrayList<AuthenticationPreProcessor>(this.authenticationPreProcessors);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication pre processors for this transaction are [{}]", list);
        return list;
    }

    @Override
    public PrincipalResolver getPrincipalResolver(final AuthenticationHandler handler,
                                                  final AuthenticationTransaction transaction) {
        return authenticationHandlerPrincipalResolverMap.get(handler);
    }

    @Override
    public Collection<AuthenticationPolicy> getAuthenticationPolicies(final AuthenticationTransaction transaction) {
        val handlerResolvers = getAuthenticationPolicyResolvers(transaction);
        LOGGER.debug("Authentication policy resolvers for this transaction are [{}]", handlerResolvers);

        val list = new ArrayList<>(this.authenticationPolicies);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Candidate authentication policies for this transaction are [{}]", list);

        val resolvedPolicies = handlerResolvers.stream()
            .filter(r -> r.supports(transaction))
            .map(r -> r.resolve(transaction))
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (resolvedPolicies.isEmpty()) {
            LOGGER.debug("Authentication policy resolvers produced no candidate authentication handler. Using default policies");
            return list;
        }
        LOGGER.debug("Resolved authentication policies are [{}]", resolvedPolicies);
        return resolvedPolicies;
    }

    @Override
    public Collection<AuthenticationPolicy> getAuthenticationPolicies(final Authentication authentication) {
        val list = new ArrayList<AuthenticationPolicy>(this.authenticationPolicies);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication policies for this assertion are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationHandlerResolver> getAuthenticationHandlerResolvers(final AuthenticationTransaction transaction) {
        val list = new ArrayList<AuthenticationHandlerResolver>(this.authenticationHandlerResolvers);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication handler resolvers for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationPolicyResolver> getAuthenticationPolicyResolvers(final AuthenticationTransaction transaction) {
        val list = new ArrayList<AuthenticationPolicyResolver>(this.authenticationPolicyResolvers);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication policy resolvers for this transaction are [{}]", list);
        return list;
    }
}
