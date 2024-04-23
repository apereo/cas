package org.apereo.cas.oidc.web.response;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import org.apereo.cas.util.DateTimeUtils;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link BaseOAuth20JwtResponseModeBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseOAuth20JwtResponseModeBuilder implements OAuth20ResponseModeBuilder {
    protected final ObjectProvider<OidcConfigurationContext> configurationContext;

    protected Date getExpirationDate() {
        val jwtExpiration = Beans.newDuration(configurationContext.getObject().getCasProperties()
            .getAuthn().getOidc().getJarm().getExpiration()).toSeconds();
        val expiration = LocalDateTime.now(Clock.systemUTC()).plusSeconds(jwtExpiration);
        return DateTimeUtils.dateOf(expiration);
    }

    protected String buildJwtResponse(final RegisteredService registeredService,
                                      final Map<String, String> parameters) {
        val ctx = configurationContext.getObject();
        val oidcService = Objects.requireNonNull((OidcRegisteredService) registeredService);
        val claimsBuilder = new JWTClaimsSet.Builder()
            .issuer(ctx.getIssuerService().determineIssuer(Optional.of(oidcService)))
            .expirationTime(getExpirationDate())
            .audience(oidcService.getClientId());
        parameters.forEach(claimsBuilder::claim);
        val claimSet = claimsBuilder.build();
        LOGGER.debug("Generating JWT response based on claims [{}]", claimSet.toString());
        return ctx.getResponseModeJwtBuilder().build(registeredService, claimSet);
    }
}
