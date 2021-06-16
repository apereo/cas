package org.apereo.cas.oidc.issuer;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.services.OidcRegisteredService;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * This is {@link OidcDefaultIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class OidcDefaultIssuerService implements OidcIssuerService {
    private final OidcProperties properties;

    @Override
    public String determineIssuer(final Optional<OidcRegisteredService> registeredService) {
        return properties.getCore().getIssuer();
    }
}
