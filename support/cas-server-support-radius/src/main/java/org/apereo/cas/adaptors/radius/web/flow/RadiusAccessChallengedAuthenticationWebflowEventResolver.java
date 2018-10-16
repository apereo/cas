package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RadiusAccessChallengedAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RadiusAccessChallengedAuthenticationWebflowEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private final String radiusMultifactorAuthenticationProviderId;

    public RadiusAccessChallengedAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                    final ServicesManager servicesManager,
                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                    final CookieGenerator warnCookieGenerator,
                                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                                    final MultifactorAuthenticationProviderSelector selector,
                                                                    final String providerId) {
        super(authenticationSystemSupport, centralAuthenticationService,
            servicesManager, ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector);
        this.radiusMultifactorAuthenticationProviderId = providerId;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService registeredService = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return null;
        }
        final Map<String, MultifactorAuthenticationProvider> providerMap =
            MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        final Principal principal = authentication.getPrincipal();
        final Map<String, Object> attributes = principal.getAttributes();
        LOGGER.debug("Evaluating principal attributes [{}] for multifactor authentication", attributes.keySet());
        if (attributes.containsKey(Attr_ReplyMessage.NAME) && attributes.containsKey(Attr_State.NAME)) {
            LOGGER.debug("Authentication requires multifactor authentication via provider [{}]", this.radiusMultifactorAuthenticationProviderId);
            final Optional<MultifactorAuthenticationProvider> providerFound = resolveProvider(providerMap, this.radiusMultifactorAuthenticationProviderId);
            if (providerFound.isPresent()) {
                final MultifactorAuthenticationProvider multifactorAuthenticationProvider = providerFound.get();
                final Event event = validateEventIdForMatchingTransitionInContext(multifactorAuthenticationProvider.getId(), context,
                    buildEventAttributeMap(authentication.getPrincipal(), registeredService, multifactorAuthenticationProvider));
                return CollectionUtils.wrapSet(event);
            }
            LOGGER.warn("No multifactor provider could be found for [{}]", this.radiusMultifactorAuthenticationProviderId);
            throw new AuthenticationException();
        }
        return null;
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
