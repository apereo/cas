package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AdaptiveMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class AdaptiveMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final GeoLocationService geoLocationService;

    private final CasConfigurationProperties casProperties;

    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    private static boolean checkUserAgentOrClientIp(@Nullable final String clientIp, @Nullable final String agent,
                                                    final String mfaMethod, final String pattern) {
        if (StringUtils.isNotBlank(agent) && RegexUtils.find(pattern, agent)) {
            LOGGER.debug("Current user agent [{}] at [{}] matches the provided pattern [{}] for "
                    + "adaptive authentication and is required to use [{}]",
                agent, clientIp, pattern, mfaMethod);
            return true;
        }

        if (StringUtils.isNotBlank(clientIp) && RegexUtils.find(pattern, clientIp)) {
            LOGGER.debug("Current client IP [{}] matches the provided pattern [{}] for "
                    + "adaptive authentication and is required to use [{}]",
                clientIp, pattern, mfaMethod);
            return true;
        }
        return false;
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   final Service service) throws Throwable {

        val multifactorMap = casProperties.getAuthn().getAdaptive().getPolicy().getRequireMultifactor();

        if (authentication == null) {
            LOGGER.trace("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        if (multifactorMap == null || multifactorMap.isEmpty()) {
            LOGGER.trace("Adaptive authentication is not configured to require multifactor authentication");
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        }

        val clientInfo = ClientInfoHolder.getClientInfo();
        val clientIp = clientInfo.getClientIpAddress();
        LOGGER.debug("Located client IP address as [{}]", clientIp);

        val agent = HttpRequestUtils.getHttpServletRequestUserAgent(httpServletRequest);

        val entries = multifactorMap.entrySet();
        for (final Map.Entry entry : entries) {
            val mfaMethod = entry.getKey().toString();
            val pattern = entry.getValue().toString();

            val providerFound = MultifactorAuthenticationUtils.resolveProvider(providerMap, mfaMethod);

            if (providerFound.isEmpty()) {
                LOGGER.error("Adaptive authentication is configured to require [{}] for [{}], yet [{}] is absent in the configuration.",
                    mfaMethod, pattern, mfaMethod);
                throw new AuthenticationException();
            }

            if (checkUserAgentOrClientIp(clientIp, agent, mfaMethod, pattern)) {
                return providerFound;
            }

            if (checkRequestGeoLocation(httpServletRequest, clientIp, mfaMethod, pattern)) {
                return providerFound;
            }
        }
        return Optional.empty();
    }

    private boolean checkRequestGeoLocation(final HttpServletRequest httpServletRequest,
                                            final String clientIp, final String mfaMethod,
                                            final String pattern) throws Throwable {
        if (this.geoLocationService == null) {
            LOGGER.trace("No geolocation service is defined");
            return false;
        }

        val location = HttpRequestUtils.getHttpServletRequestGeoLocation(httpServletRequest);
        val loc = this.geoLocationService.locate(clientIp, location);
        if (loc == null) {
            LOGGER.trace("No geolocation response is provided");
            return false;
        }

        val address = loc.build();
        if (RegexUtils.find(pattern, address)) {
            LOGGER.debug("Current address [{}] at [{}] matches the provided pattern [{}] for "
                    + "adaptive authentication and is required to use [{}]",
                address, clientIp, pattern, mfaMethod);
            return true;
        }
        return false;
    }
}
