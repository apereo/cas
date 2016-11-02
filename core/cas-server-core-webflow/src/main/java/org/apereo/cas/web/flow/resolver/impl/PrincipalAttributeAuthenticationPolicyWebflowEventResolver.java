package org.apereo.cas.web.flow.resolver.impl;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link PrincipalAttributeAuthenticationPolicyWebflowEventResolver}
 * that attempts to locate a principal attribute, match its value against
 * the provided pattern and decide the next event in the flow for the given service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PrincipalAttributeAuthenticationPolicyWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        if (StringUtils.isBlank(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers())) {
            logger.debug("Attribute name to determine event is not configured for {}", principal.getId());
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            return null;
        }

        final Collection<MultifactorAuthenticationProvider> providers = providerMap.values();
        if (providers.size() == 1 && StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeValueRegex())) {
            final MultifactorAuthenticationProvider provider = providers.iterator().next();
            logger.debug("Found a single multifactor provider {} in the application context", provider);
            return resolveEventViaPrincipalAttribute(principal,
                    org.springframework.util.StringUtils.commaDelimitedListToSet(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers()),
                    service, context, providers,
                    input -> input.toString().matches(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeValueRegex()));
        }
        
        return resolveEventViaPrincipalAttribute(principal,
                org.springframework.util.StringUtils.commaDelimitedListToSet(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers()),
                service, context, providers,
                input -> providers.stream()
                        .filter(provider -> provider.getId().equals(input))
                        .count() > 0);
    }

}
