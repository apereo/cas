package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@RequiredArgsConstructor
@Accessors(chain = true)
public class DefaultAuthenticationEventExecutionPlan implements AuthenticationEventExecutionPlan {
    private final List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulatorList = new ArrayList<>(0);

    private final List<AuthenticationPostProcessor> authenticationPostProcessors = new ArrayList<>(0);

    private final List<AuthenticationPreProcessor> authenticationPreProcessors = new ArrayList<>(0);

    private final List<AuthenticationPolicy> authenticationPolicies = new ArrayList<>(0);

    private final List<AuthenticationHandlerResolver> authenticationHandlerResolvers = new ArrayList<>(0);

    private final List<AuthenticationPolicyResolver> authenticationPolicyResolvers = new ArrayList<>(0);

    private final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlerPrincipalResolverMap = new LinkedHashMap<>();

    @Setter
    private AuthenticationHandlerResolver defaultAuthenticationHandlerResolver = new DefaultAuthenticationHandlerResolver();

    @Override
    public boolean registerAuthenticationHandler(final AuthenticationHandler handler) {
        return registerAuthenticationHandlerWithPrincipalResolver(handler, null);
    }

    @Override
    public void registerAuthenticationMetadataPopulator(final AuthenticationMetaDataPopulator populator) {
        if (BeanSupplier.isNotProxy(populator)) {
            LOGGER.trace("Registering metadata populator [{}] into the execution plan", populator);
            authenticationMetaDataPopulatorList.add(populator);
        }
    }

    @Override
    public void registerAuthenticationPostProcessor(final AuthenticationPostProcessor processor) {
        if (BeanSupplier.isNotProxy(processor)) {
            LOGGER.debug("Registering authentication post processor [{}] into the execution plan", processor);
            authenticationPostProcessors.add(processor);
        }
    }

    @Override
    public void registerAuthenticationPreProcessor(final AuthenticationPreProcessor processor) {
        if (BeanSupplier.isNotProxy(processor)) {
            LOGGER.debug("Registering authentication pre processor [{}] into the execution plan", processor);
            authenticationPreProcessors.add(processor);
        }
    }

    @Override
    public void registerAuthenticationMetadataPopulators(final Collection<AuthenticationMetaDataPopulator> populators) {
        populators.stream().filter(BeanSupplier::isNotProxy).forEach(this::registerAuthenticationMetadataPopulator);
    }

    @Override
    public void registerAuthenticationPolicy(final AuthenticationPolicy authenticationPolicy) {
        if (BeanSupplier.isNotProxy(authenticationPolicy)) {
            this.authenticationPolicies.add(authenticationPolicy);
        }
    }

    @Override
    public void registerAuthenticationPolicies(final Collection<AuthenticationPolicy> authenticationPolicy) {
        this.authenticationPolicies.addAll(authenticationPolicy.stream().filter(BeanSupplier::isNotProxy).toList());
    }

    @Override
    public void registerAuthenticationHandlerResolver(final AuthenticationHandlerResolver handlerResolver) {
        if (BeanSupplier.isNotProxy(handlerResolver)) {
            this.authenticationHandlerResolvers.add(handlerResolver);
        }
    }

    @Override
    public void registerAuthenticationPolicyResolver(final AuthenticationPolicyResolver policyResolver) {
        if (BeanSupplier.isNotProxy(policyResolver)) {
            this.authenticationPolicyResolvers.add(policyResolver);
        }
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final Map<AuthenticationHandler, PrincipalResolver> plan) {
        plan.forEach(this::registerAuthenticationHandlerWithPrincipalResolver);
    }

    @Override
    public boolean registerAuthenticationHandlerWithPrincipalResolver(final AuthenticationHandler handler,
                                                                      final PrincipalResolver principalResolver) {
        return FunctionUtils.doIf(BeanSupplier.isNotProxy(handler), () -> {
            LOGGER.trace("Registering handler [{}] with [{}] principal resolver into the execution plan",
                handler.getName(), Optional.ofNullable(principalResolver).map(PrincipalResolver::getName).orElse("no"));

            if (authenticationHandlerPrincipalResolverMap.containsKey(handler)) {
                LOGGER.error("Authentication execution plan has found an existing handler [{}]. "
                        + "Attempts to register a new authentication handler with the same name may lead to unpredictable results. "
                        + "Please make sure all authentication handlers are uniquely defined/named in the CAS configuration.",
                    handler.getName());
                return false;
            }
            authenticationHandlerPrincipalResolverMap.put(handler, principalResolver);
            return true;
        }, () -> false).get();
    }

    @Override
    public void registerAuthenticationHandlersWithPrincipalResolver(final Collection<AuthenticationHandler> handlers,
                                                                    final PrincipalResolver principalResolver) {
        handlers.stream().filter(BeanSupplier::isNotProxy)
            .forEach(h -> registerAuthenticationHandlerWithPrincipalResolver(h, principalResolver));
    }

    @Override
    public void registerAuthenticationHandlersWithPrincipalResolver(final List<AuthenticationHandler> handlers,
                                                                    final List<PrincipalResolver> principalResolver) {
        if (handlers.size() != principalResolver.size()) {
            LOGGER.error("Total number of authentication handlers must match the number of provided principal resolvers");
            return;
        }
        IntStream.range(0, handlers.size())
            .forEach(i -> registerAuthenticationHandlerWithPrincipalResolver(handlers.get(i), principalResolver.get(i)));
    }

    @Override
    public @NonNull Set<AuthenticationHandler> getAuthenticationHandlers(final AuthenticationTransaction transaction) throws Throwable {
        val handlers = getAuthenticationHandlers();
        LOGGER.debug("Candidate/Registered authentication handlers for this transaction [{}] are [{}]", transaction, handlers);
        val handlerResolvers = getAuthenticationHandlerResolvers(transaction);
        LOGGER.debug("Authentication handler resolvers for this transaction are [{}]", handlerResolvers);

        val resolvedHandlers = handlerResolvers.stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(Unchecked.predicate(r -> r.supports(handlers, transaction)))
            .map(Unchecked.function(r -> r.resolve(handlers, transaction)))
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (resolvedHandlers.isEmpty()) {
            LOGGER.debug("Authentication handler resolvers produced no candidate authentication handler. Using the default handler resolver instead...");
            if (defaultAuthenticationHandlerResolver.supports(handlers, transaction)) {
                resolvedHandlers.addAll(defaultAuthenticationHandlerResolver.resolve(handlers, transaction));
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
        val list = new ArrayList<>(this.authenticationPreProcessors);
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

        val list = getAuthenticationPolicies();
        val resolvedPolicies = handlerResolvers.stream()
            .filter(Unchecked.predicate(r -> r.supports(transaction)))
            .map(Unchecked.function(r -> r.resolve(transaction)))
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (resolvedPolicies.isEmpty()) {
            LOGGER.debug("Authentication policy resolvers produced no candidate authentication policy. Using default policies");
            return list;
        }
        LOGGER.debug("Resolved authentication policies are [{}]", resolvedPolicies);
        return resolvedPolicies;
    }

    @Override
    public Collection<AuthenticationPolicy> getAuthenticationPolicies(final Authentication authentication) {
        val list = new ArrayList<>(this.authenticationPolicies);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication policies for this assertion are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationPolicy> getAuthenticationPolicies() {
        val list = new ArrayList<>(this.authenticationPolicies);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Candidate authentication policies for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationHandlerResolver> getAuthenticationHandlerResolvers(final AuthenticationTransaction transaction) {
        val list = new ArrayList<>(this.authenticationHandlerResolvers);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication handler resolvers for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationPolicyResolver> getAuthenticationPolicyResolvers(final AuthenticationTransaction transaction) {
        val list = new ArrayList<>(this.authenticationPolicyResolvers);
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Sorted and registered authentication policy resolvers for this transaction are [{}]", list);
        return list;
    }
}
