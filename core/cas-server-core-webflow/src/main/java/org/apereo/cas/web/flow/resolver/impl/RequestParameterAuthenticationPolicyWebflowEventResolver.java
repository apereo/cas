package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.ImmutableSet;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RequestParameterAuthenticationPolicyWebflowEventResolver}
 * that attempts to resolve the next event based on the authentication providers of this service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RequestParameterAuthenticationPolicyWebflowEventResolver extends AbstractCasWebflowEventResolver {

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
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final String[] values = request.getParameterValues(casProperties.getAuthn().getMfa().getRequestParameter());
        if (values != null && values.length > 0) {
            logger.debug("Received request parameter {} as {}", casProperties.getAuthn().getMfa().getRequestParameter(), values);

            final Map<String, MultifactorAuthenticationProvider> providerMap =
                    WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
            if (providerMap == null || providerMap.isEmpty()) {
                logger.warn("No multifactor authentication providers are available in the application context to satisfy {}", (Object[]) values);
                throw new AuthenticationException();
            }

            final Optional<MultifactorAuthenticationProvider> providerFound = providerMap.values().stream()
                    .filter(provider -> provider.getId().equals(values[0]))
                    .findFirst();

            if (providerFound.isPresent()) {
                if (providerFound.get().isAvailable(service)) {
                    logger.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                            providerFound.get(), service.getName());
                    final Event event = validateEventIdForMatchingTransitionInContext(providerFound.get().getId(), context,
                            buildEventAttributeMap(authentication.getPrincipal(), service, providerFound.get()));
                    return ImmutableSet.of(event);
                }
                logger.warn("Located multifactor provider {}, yet the provider cannot be reached or verified", providerFound.get());
                return null;
            } else {
                logger.warn("No multifactor provider could be found for request parameter {}", (Object[]) values);
                throw new AuthenticationException();
            }
        }
        logger.debug("No value could be found for request parameter {}", casProperties.getAuthn().getMfa().getRequestParameter());
        return null;
    }
}
