package org.apereo.cas.oidc.issuer;

import org.apereo.cas.oidc.AbstractOidcTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultIssuerServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
public class OidcDefaultIssuerServiceTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        assertNotNull(oidcIssuerService.determineIssuer(Optional.empty()));
    }
}
