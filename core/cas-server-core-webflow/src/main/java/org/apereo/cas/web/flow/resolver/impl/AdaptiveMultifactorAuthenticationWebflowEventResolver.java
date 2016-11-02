package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.ImmutableSet;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link AdaptiveMultifactorAuthenticationWebflowEventResolver},
 * which handles the initial authentication attempt and calls upon a number of
 * embedded resolvers to produce the next event in the authentication flow.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AdaptiveMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    private GeoLocationService geoLocationService;
    
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
        
        final Map multifactorMap = casProperties.getAuthn().getAdaptive().getRequireMultifactor();
        if (multifactorMap == null || multifactorMap.isEmpty()) {
            logger.debug("Adaptive authentication is not configured to require multifactor authentication");
            return null;
        }
        
        final Map<String, MultifactorAuthenticationProvider> providerMap = 
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }
        
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final String clientIp = clientInfo.getClientIpAddress();
        logger.debug("Located client IP address as [{}]", clientIp);

        final String agent = WebUtils.getHttpServletRequestUserAgent();

        final Set<Map.Entry> entries = multifactorMap.entrySet();
        for (final Map.Entry entry : entries) {
            final String mfaMethod = entry.getKey().toString();
            final String pattern = entry.getValue().toString();

            final Optional<MultifactorAuthenticationProvider> providerFound = providerMap.values().stream()
                    .filter(provider -> provider.getId().equals(mfaMethod))
                    .findFirst();
            
            if (!providerFound.isPresent()) {
                logger.error("Adaptive authentication is configured to require [{}] for [{}], yet [{}] is absent in the configuration.",
                            mfaMethod, pattern, mfaMethod);
                throw new AuthenticationException();
            }
            
            if (agent.matches(pattern) || clientIp.matches(pattern)) {
                logger.debug("Current user agent [{}] at [{}] matches the provided pattern {} for "
                             + "adaptive authentication and is required to use [{}]",
                            agent, clientIp, pattern, mfaMethod);

                return buildEvent(context, service, authentication, providerFound.get());
            }
                        
            if (this.geoLocationService != null) {
                final GeoLocationRequest location = WebUtils.getHttpServletRequestGeoLocation();
                final GeoLocationResponse loc = this.geoLocationService.locate(clientIp, location);
                if (loc != null) {
                    final String address = loc.buildAddress();
                    if (address.matches(pattern)) {
                        logger.debug("Current address [{}] at [{}] matches the provided pattern {} for "
                                        + "adaptive authentication and is required to use [{}]",
                                address, clientIp, pattern, mfaMethod);
                        return buildEvent(context, service, authentication, providerFound.get());
                    }
                }
            }
        }
        return null;
    }

    private Set<Event> buildEvent(final RequestContext context, final RegisteredService service, 
                                  final Authentication authentication, 
                                  final MultifactorAuthenticationProvider provider) {
        if (provider.isAvailable(service)) {
            logger.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                    provider, service.getName());
            final Event event = validateEventIdForMatchingTransitionInContext(provider.getId(), context,
                    buildEventAttributeMap(authentication.getPrincipal(), service, provider));
            return ImmutableSet.of(event);
        }
        logger.warn("Located multifactor provider [{}], yet the provider cannot be reached or verified", provider);
        return null;
    }

    public void setGeoLocationService(final GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }
}
