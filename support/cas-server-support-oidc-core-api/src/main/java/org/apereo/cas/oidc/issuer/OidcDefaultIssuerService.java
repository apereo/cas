package org.apereo.cas.oidc.issuer;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * This is {@link OidcDefaultIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcDefaultIssuerService implements OidcIssuerService {
    private final OidcProperties properties;

    @Override
    public String determineIssuer(final Optional<OidcRegisteredService> registeredService) {
        val issuer = registeredService
            .filter(svc -> StringUtils.isNotBlank(svc.getIdTokenIssuer()))
            .map(OidcRegisteredService::getIdTokenIssuer)
            .orElse(properties.getCore().getIssuer());
        LOGGER.trace("Determined issuer as [{}] for [{}]", issuer,
            registeredService.map(RegisteredService::getName).orElse("CAS"));
        return StringUtils.removeEnd(issuer, "/");
    }
}
