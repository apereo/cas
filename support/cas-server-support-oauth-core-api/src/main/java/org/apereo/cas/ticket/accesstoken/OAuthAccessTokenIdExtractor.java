package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link OAuthAccessTokenIdExtractor}.
 *
 * @author charlibot
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class OAuthAccessTokenIdExtractor {

    private final String casSeverPrefix;
    private final CipherExecutor<Serializable, String> defaultTokenCipherExecutor;
    private final ServicesManager servicesManager;
    private final RegisteredServiceCipherExecutor registeredServiceCipherExecutor;

    @SneakyThrows
    public String extractId(final String accessTokenJwt) {
        String decoded = null;
        if (defaultTokenCipherExecutor.isEnabled()) {
            LOGGER.trace("Verifying JWT based on default global keys for [{}]", accessTokenJwt);
            try {
                decoded = defaultTokenCipherExecutor.decode(accessTokenJwt);
            } catch (final Exception e) {
                LOGGER.trace("Not encoded by the default", e);
            }
        }

        if (StringUtils.isEmpty(decoded)) {
            val services = servicesManager.findServiceBy(OAuthRegisteredService.class::isInstance);
            for (val service : services) {
                if (registeredServiceCipherExecutor.supports(service)) {
                    try {
                        decoded = registeredServiceCipherExecutor.decode(accessTokenJwt, Optional.of(service));
                        break;
                    } catch (final Exception e) {
                        LOGGER.trace("Not encoded by the service [{}]", service.getServiceId());
                    }
                }
            }
        }

        if (StringUtils.isEmpty(decoded)) {
            throw new IllegalArgumentException("Could not parse the access token");
        }

        val claims = JwtClaims.parse(decoded);
        if (!claims.getIssuer().equals(casSeverPrefix)) {
            throw new IllegalArgumentException("Issuer from jwt not equal to the cas.server.prefix");
        }

        val serviceAudience = claims.getAudience().get(0);
        LOGGER.trace("Locating service [{}] in service registry", serviceAudience);
        val registeredService = locateRegisteredService(serviceAudience);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        return claims.getJwtId();
    }

    private RegisteredService locateRegisteredService(final String serviceAudience) {
        var service = servicesManager.findServiceBy(serviceAudience);
        if (service == null) {
            service = OAuth20Utils.getRegisteredOAuthServiceByClientId(getServicesManager(), serviceAudience);
        }
        return service;
    }
}
