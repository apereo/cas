package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.URIBuilder;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link OidcMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class OidcMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;
    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService, final HttpServletRequest request, final Service service) {
        if (registeredService == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }

        var acr = request.getParameter(OAuth20Constants.ACR_VALUES);
        if (StringUtils.isBlank(acr)) {
            val builderContext = new URIBuilder(request.getRequestURI());
            val parameter = builderContext.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(OAuth20Constants.ACR_VALUES))
                .findFirst();
            if (parameter.isPresent()) {
                acr = parameter.get().getValue();
            }
        }
        if (StringUtils.isBlank(acr)) {
            LOGGER.debug("No ACR provided in the authentication request");
            return Optional.empty();
        }
        val values = org.springframework.util.StringUtils.commaDelimitedListToSet(acr);
        if (values.isEmpty()) {
            LOGGER.debug("No ACR provided in the authentication request");
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(ApplicationContextProvider.getApplicationContext());
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context to handle [{}]", values);
            throw new AuthenticationException();
        }

        return providerMap.values()
            .stream()
            .filter(v -> values.contains(v.getId()))
            .findAny();
    }
}
