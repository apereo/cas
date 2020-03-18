package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link TimedMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class TimedMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;
    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final Service service) {

        val timedMultifactor = casProperties.getAuthn().getAdaptive().getRequireTimedMultifactor();
        if (service == null || authentication == null) {
            LOGGER.trace("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }

        if (timedMultifactor == null || timedMultifactor.isEmpty()) {
            LOGGER.trace("Adaptive authentication is not configured to require multifactor authentication by time");
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        }

        return checkTimedMultifactorProvidersForRequest(registeredService);
    }

    /**
     * Check timed multifactor providers for request optional.
     *
     * @param service        the service
     * @return the provider
     */
    protected Optional<MultifactorAuthenticationProvider> checkTimedMultifactorProvidersForRequest(final RegisteredService service) {

        val timedMultifactor = casProperties.getAuthn().getAdaptive().getRequireTimedMultifactor();
        val now = LocalDateTime.now(ZoneId.systemDefault());
        val dow = DayOfWeek.from(now);
        val dayNamesForToday = Arrays.stream(TextStyle.values())
            .map(style -> dow.getDisplayName(style, Locale.getDefault()))
            .collect(Collectors.toList());

        val timed = timedMultifactor.stream()
            .filter(t -> {
                var providerEvent = false;
                if (!t.getOnDays().isEmpty()) {
                    providerEvent = t.getOnDays().stream().anyMatch(dayNamesForToday::contains);
                }
                if (t.getOnOrAfterHour() >= 0) {
                    providerEvent = now.getHour() >= t.getOnOrAfterHour();
                }
                if (t.getOnOrBeforeHour() >= 0) {
                    providerEvent = now.getHour() <= t.getOnOrBeforeHour();
                }
                return providerEvent;
            })
            .findFirst()
            .orElse(null);

        if (timed != null) {
            val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
            val providerFound = MultifactorAuthenticationUtils.resolveProvider(providerMap, timed.getProviderId());
            if (providerFound.isEmpty()) {
                LOGGER.error("Adaptive authentication is configured to require [{}] for [{}], yet [{}] absent in the configuration.",
                    timed.getProviderId(), service, timed.getProviderId());
                throw new AuthenticationException();
            }
            return providerFound;
        }
        return Optional.empty();
    }
}
