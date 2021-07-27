package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    private static String getAuthenticationClassReference(final HttpServletRequest request) throws URISyntaxException {
        var acr = request.getParameter(OAuth20Constants.ACR_VALUES);
        if (StringUtils.isBlank(acr)) {
            val serviceParam = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
            if (StringUtils.isNotBlank(serviceParam)) {
                val queryParams = new URIBuilder(UriComponentsBuilder.fromUriString(serviceParam).toUriString()).getQueryParams();
                val parameter = queryParams
                    .stream()
                    .filter(p -> p.getName().equals(OAuth20Constants.ACR_VALUES))
                    .findFirst();
                if (parameter.isPresent()) {
                    return EncodingUtils.urlDecode(parameter.get().getValue());
                }
            }
        }
        return EncodingUtils.urlDecode(acr);
    }

    @Override
    @SneakyThrows
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest request,
                                                                   final Service service) {
        val acr = getAuthenticationClassReference(request);
        if (StringUtils.isBlank(acr)) {
            LOGGER.debug("No ACR provided in the authentication request");
            return Optional.empty();
        }
        val values = List.of(org.springframework.util.StringUtils.delimitedListToStringArray(acr, " "));
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context to handle [{}]", values);
            throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        }

        val authnContexts = casProperties.getAuthn().getOidc().getCore().getAuthenticationContextReferenceMappings();
        val mappings = CollectionUtils.convertDirectedListToMap(authnContexts);
        val mappedAcrValues = values
            .stream()
            .map(acrValue -> mappings.getOrDefault(acrValue, acrValue))
            .collect(Collectors.toList());
        LOGGER.debug("Mapped ACR values are [{}] to compare against [{}]", mappedAcrValues, providerMap.values());
        return providerMap.values()
            .stream()
            .filter(v -> mappedAcrValues.contains(v.getId()))
            .findAny();
    }
}
