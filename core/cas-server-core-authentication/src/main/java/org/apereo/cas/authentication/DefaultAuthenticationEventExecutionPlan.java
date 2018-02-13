package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationEventExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultAuthenticationEventExecutionPlan implements AuthenticationEventExecutionPlan {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationEventExecutionPlan.class);

    private final List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulatorList = new ArrayList<>();
    private final List<AuthenticationPostProcessor> authenticationPostProcessors = new ArrayList<>();
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
        final List<AuthenticationMetaDataPopulator> list = new ArrayList(this.authenticationMetaDataPopulatorList);
        OrderComparator.sort(list);
        LOGGER.debug("Sorted and registered metadata populators for this transaction are [{}]", list);
        return list;
    }

    @Override
    public Set<AuthenticationHandler> getAuthenticationHandlersForTransaction(final AuthenticationTransaction transaction) {
        final AuthenticationHandler[] handlers = authenticationHandlerPrincipalResolverMap.keySet().toArray(new AuthenticationHandler[]{});
        OrderComparator.sortIfNecessary(handlers);
        return new LinkedHashSet<>(CollectionUtils.wrapList(handlers));
    }

    @Override
    public PrincipalResolver getPrincipalResolverForAuthenticationTransaction(final AuthenticationHandler handler,
                                                                              final AuthenticationTransaction transaction) {
        return authenticationHandlerPrincipalResolverMap.get(handler);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolvers(final Collection<AuthenticationHandler> handlers,
                                                                    final PrincipalResolver principalResolver) {
        handlers.forEach(h -> registerAuthenticationHandlerWithPrincipalResolver(h, principalResolver));
    }

    @Override
    public void registerAuthenticationPostProcessor(final AuthenticationPostProcessor processor) {
        LOGGER.debug("Registering authentication post processor [{}] into the execution plan", processor);
        authenticationPostProcessors.add(processor);
    }

    @Override
    public Collection<AuthenticationPostProcessor> getAuthenticationPostProcessors(final AuthenticationTransaction transaction) {
        final List<AuthenticationPostProcessor> list = new ArrayList(this.authenticationPostProcessors);
        OrderComparator.sort(list);
        LOGGER.debug("Sorted and registered authentication post processors for this transaction are [{}]", list);
        return list;
    }
}
