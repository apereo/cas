package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This is {@link PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver
    extends PrincipalAttributeMultifactorAuthenticationPolicyEventResolver {

    private static final Class[] PREDICATE_CTOR_PARAMETERS = {Object.class, Object.class, Object.class, Object.class};

    private final Resource predicateResource;

    public PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                                    final ServicesManager servicesManager,
                                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                                    final CookieGenerator warnCookieGenerator,
                                                                                    final AuthenticationServiceSelectionPlan authSelectionStrategies,
                                                                                    final MultifactorAuthenticationProviderSelector selector,
                                                                                    final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
            ticketRegistrySupport, warnCookieGenerator, authSelectionStrategies, selector,
            casProperties);
        predicateResource = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributePredicate();
    }

    @Override
    @SneakyThrows
    protected Set<Event> resolveMultifactorProviderViaPredicate(final RequestContext context,
                                                                final RegisteredService service,
                                                                final Principal principal,
                                                                final Collection<MultifactorAuthenticationProvider> providers) {

        if (predicateResource == null || !ResourceUtils.doesResourceExist(predicateResource)) {
            LOGGER.debug("No groovy script predicate is defined to decide which multifactor authentication provider should be chosen");
            return null;
        }

        final Object[] args = {service, principal, providers, LOGGER};
        final Predicate<MultifactorAuthenticationProvider> predicate =
            ScriptingUtils.getObjectInstanceFromGroovyResource(predicateResource, PREDICATE_CTOR_PARAMETERS,
                args, Predicate.class);

        LOGGER.debug("Created predicate instance [{}] from [{}] to filter multifactor authentication providers [{}]",
            predicate.getClass().getSimpleName(), predicateResource, providers);

        if (providers == null || providers.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return null;
        }

        val provider = providers
            .stream()
            .filter(predicate)
            .min(Comparator.comparingInt(MultifactorAuthenticationProvider::getOrder))
            .orElse(null);

        LOGGER.debug("Predicate instance [{}] returned multifactor authentication provider [{}]", predicate.getClass().getSimpleName(), provider);
        return evaluateEventForProviderInContext(principal, service, context, provider);

    }
}
