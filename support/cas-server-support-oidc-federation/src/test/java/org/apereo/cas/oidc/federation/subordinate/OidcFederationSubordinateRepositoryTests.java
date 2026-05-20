package org.apereo.cas.oidc.federation.subordinate;

import module java.base;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcFederationSubordinateRepositoryTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("OIDCServices")
class OidcFederationSubordinateRepositoryTests extends AbstractOidcTrustAnchorFederationTests {

    @Autowired
    @Qualifier("oidcFederationSubordinateRepository")
    private OidcFederationSubordinateRepository oidcFederationSubordinateRepository;

    @Test
    public void verifyOperation() {
        assertEquals(5, oidcFederationSubordinateRepository.getSubordinates().size());
    }
}
