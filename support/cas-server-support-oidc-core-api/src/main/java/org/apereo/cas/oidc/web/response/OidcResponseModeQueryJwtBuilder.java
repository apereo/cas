package org.apereo.cas.oidc.web.response;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import org.apereo.cas.util.DateTimeUtils;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link OidcResponseModeQueryJwtBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcResponseModeQueryJwtBuilder implements OAuth20ResponseModeBuilder {
    private final ObjectProvider<OidcConfigurationContext> configurationContext;

    @Override
    public ModelAndView build(final RegisteredService registeredService,
                              final String redirectUrl, final Map<String, String> parameters) throws Exception {
        return configurationContext
            .stream()
            .map(ctx -> {
                val oidcService = Objects.requireNonNull((OidcRegisteredService) registeredService);
                val claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(ctx.getIssuerService().determineIssuer(Optional.of(oidcService)))
                    .expirationTime(DateTimeUtils.dateOf(LocalDateTime.now(Clock.systemUTC()).plusMinutes(5)))
                    .audience(oidcService.getClientId());
                parameters.forEach(claimsBuilder::claim);
                val claimSet = claimsBuilder.build();
                LOGGER.debug("Generating JWT response based on claims [{}]", claimSet.toString());
                val token = ctx.getAccessTokenJwtBuilder().build(registeredService, claimSet);
                val mv = new RedirectView(redirectUrl);
                return new ModelAndView(mv, Map.of("response", token));
            })
            .findFirst()
            .orElseThrow();
    }

    @Override
    public OAuth20ResponseModeTypes getResponseMode() {
        return OAuth20ResponseModeTypes.QUERY_JWT;
    }
}
