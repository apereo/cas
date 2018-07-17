package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * This is {@link DefaultAuthenticationEventExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DefaultAuthenticationEventExecutionPlan implements AuthenticationEventExecutionPlan {
    private final List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulatorList = new ArrayList<>();
    private final List<AuthenticationPostProcessor> authenticationPostProcessors = new ArrayList<>();
    private final List<AuthenticationPreProcessor> authenticationPreProcessors = new ArrayList<>();

    private final List<AuthenticationPolicy> authenticationPolicies = new ArrayList<>();
    private final List<AuthenticationHandlerResolver> authenticationHandlerResolvers = new ArrayList<>();

    private final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlerPrincipalResolverMap = new LinkedHashMap<>();

    @Override
    public void registerAuthenticationHandler(final AuthenticationHandler handler) {
        registerAuthenticationHandlerWithPrincipalResolver(handler, null);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final AuthenticationHandler handler, final PrincipalResolver principalResolver) {
        if (principalResolver == null) {
            LOGGER.debug("Registering handler [{}] with no principal resolver into the execution plan", handler.getName());
        } else {
            LOGGER.debug("Registering handler [{}] principal resolver [{}] into the execution plan", handler.getName(), principalResolver);
        }
        this.authenticationHandlerPrincipalResolverMap.put(handler, principalResolver);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final Map<AuthenticationHandler, PrincipalResolver> plan) {
        plan.forEach(this::registerAuthenticationHandlerWithPrincipalResolver);
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
    public void registerMetadataPopulator(final AuthenticationMetaDataPopulator populator) {
        LOGGER.debug("Registering metadata populator [{}] into the execution plan", populator);
        authenticationMetaDataPopulatorList.add(populator);
    }

    @Override
    public void registerMetadataPopulators(final Collection<AuthenticationMetaDataPopulator> populators) {
        populators.forEach(this::registerMetadataPopulator);
    }

    @Override
    public Collection<AuthenticationMetaDataPopulator> getAuthenticationMetadataPopulators(final AuthenticationTransaction transaction) {
        val list = new ArrayList(this.authenticationMetaDataPopulatorList);
        OrderComparator.sort(list);
        LOGGER.debug("Sorted and registered metadata populators for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Set<AuthenticationHandler> getAuthenticationHandlersForTransaction(final AuthenticationTransaction transaction) {
        val handlers = authenticationHandlerPrincipalResolverMap.keySet().toArray(new AuthenticationHandler[]{});
        OrderComparator.sortIfNecessary(handlers);
        return new LinkedHashSet<>(CollectionUtils.wrapList(handlers));
    }

    @Override
    public PrincipalResolver getPrincipalResolverForAuthenticationTransaction(final AuthenticationHandler handler,
                                                                              final AuthenticationTransaction transaction) {
        return authenticationHandlerPrincipalResolverMap.get(handler);
    }

    @Override
    public void registerAuthenticationPostProcessor(final AuthenticationPostProcessor processor) {
        LOGGER.debug("Registering authentication post processor [{}] into the execution plan", processor);
        authenticationPostProcessors.add(processor);
    }

    @Override
    public Collection<AuthenticationPostProcessor> getAuthenticationPostProcessors(final AuthenticationTransaction transaction) {
        val list = new ArrayList(this.authenticationPostProcessors);
        OrderComparator.sort(list);
        LOGGER.debug("Sorted and registered authentication post processors for this transaction are [{}]", list);
        return list;
    }

    @Override
    public void registerAuthenticationPreProcessor(final AuthenticationPreProcessor processor) {
        LOGGER.debug("Registering authentication pre processor [{}] into the execution plan", processor);
        authenticationPreProcessors.add(processor);
    }

    @Override
    public Collection<AuthenticationPreProcessor> getAuthenticationPreProcessors(final AuthenticationTransaction transaction) {
        val list = new ArrayList(this.authenticationPreProcessors);
        OrderComparator.sort(list);
        LOGGER.debug("Sorted and registered authentication pre processors for this transaction are [{}]", list);
        return list;
    }

    @Override
    public void registerAuthenticationPolicy(final AuthenticationPolicy authenticationPolicy) {
        this.authenticationPolicies.add(authenticationPolicy);
    }

    @Override
    public void registerAuthenticationHandlerResolver(final AuthenticationHandlerResolver handlerResolver) {
        this.authenticationHandlerResolvers.add(handlerResolver);
    }

    @Override
    public Collection<AuthenticationPolicy> getAuthenticationPolicies(final AuthenticationTransaction transaction) {
        val list = new ArrayList(this.authenticationPolicies);
        OrderComparator.sort(list);
        LOGGER.debug("Sorted and registered authentication policies for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Collection<AuthenticationHandlerResolver> getAuthenticationHandlerResolvers(final AuthenticationTransaction transaction) {
        val list = new ArrayList(this.authenticationHandlerResolvers);
        OrderComparator.sort(list);
        LOGGER.debug("Sorted and registered authentication handler resolvers for this transaction are [{}]", list);
        return list;
    }
}
