package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractCasMultifactorAuthenticationWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle MFA webflow changes.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@Slf4j
public abstract class AbstractCasMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    /**
     * The mfa selector.
     */
    protected final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    public AbstractCasMultifactorAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                    final ServicesManager servicesManager,
                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                    final CookieGenerator warnCookieGenerator,
                                                                    final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                                    final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
            warnCookieGenerator, authenticationRequestServiceSelectionStrategies);
        this.multifactorAuthenticationProviderSelector = multifactorAuthenticationProviderSelector;
    }

    /**
     * Gets authentication provider for service.
     *
     * @param service the service
     * @return the authentication provider for service
     */
    protected Collection<MultifactorAuthenticationProvider> getAuthenticationProviderForService(final RegisteredService service) {
        val policy = service.getMultifactorPolicy();
        if (policy != null) {
            return policy.getMultifactorAuthenticationProviders().stream()
                .map(this::getMultifactorAuthenticationProviderFromApplicationContext)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
        }
        return null;
    }

    /**
     * Find the MultifactorAuthenticationProvider in the application contact that matches the specified providerId (e.g. "mfa-duo").
     *
     * @param providerId the provider id
     * @return the registered service multifactor authentication provider
     */
    protected Optional<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviderFromApplicationContext(final String providerId) {
        try {
            LOGGER.debug("Locating bean definition for [{}]", providerId);
            return MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values().stream()
                .filter(p -> p.matches(providerId))
                .findFirst();
        } catch (final Exception e) {
            LOGGER.debug("Could not locate [{}] bean id in the application context as an authentication provider.", providerId);
        }
        return Optional.empty();
    }
}
