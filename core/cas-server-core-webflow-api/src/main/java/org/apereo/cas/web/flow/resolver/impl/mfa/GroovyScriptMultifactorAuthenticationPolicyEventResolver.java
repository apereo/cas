package org.apereo.cas.web.flow.resolver.impl.mfa;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.core.io.Resource;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationPolicyEventResolver}
 * that conditionally evaluates a groovy script to resolve the mfa provider id
 * and event.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class GroovyScriptMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private final transient Resource groovyScript;

    public GroovyScriptMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                    final ServicesManager servicesManager,
                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                    final CookieGenerator warnCookieGenerator,
                                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                                    final MultifactorAuthenticationProviderSelector selector,
                                                                    final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
        groovyScript = casProperties.getAuthn().getMfa().getGroovyScript();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final Service service = resolveServiceFromAuthenticationRequest(context);
        final RegisteredService registeredService = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (groovyScript == null) {
            LOGGER.debug("No groovy script is configured for multifactor authentication");
            return null;
        }

        if (!ResourceUtils.doesResourceExist(groovyScript)) {
            LOGGER.warn("No groovy script is found at [{}] for multifactor authentication", groovyScript);
            return null;
        }
        
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return null;
        }
        if (registeredService == null || service == null) {
            LOGGER.debug("No registered service is available to determine event for principal [{}]", authentication.getPrincipal());
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        try {
            final Object[] args = {service, registeredService, authentication, LOGGER};
            final String provider = ScriptingUtils.executeGroovyScript(groovyScript, args, String.class);
            LOGGER.debug("Groovy script run for [{}] returned the provider id [{}]", service, provider);
            if (StringUtils.isBlank(provider)) {
                return null;
            }

            final Optional<MultifactorAuthenticationProvider> providerFound = resolveProvider(providerMap, provider);
            if (providerFound.isPresent()) {
                final MultifactorAuthenticationProvider multifactorAuthenticationProvider = providerFound.get();
                final Event event = validateEventIdForMatchingTransitionInContext(multifactorAuthenticationProvider.getId(), context,
                        buildEventAttributeMap(authentication.getPrincipal(), registeredService, multifactorAuthenticationProvider));
                return CollectionUtils.wrapSet(event);
            }
            LOGGER.warn("No multifactor provider could be found for [{}]", provider);
            throw new AuthenticationException();

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
