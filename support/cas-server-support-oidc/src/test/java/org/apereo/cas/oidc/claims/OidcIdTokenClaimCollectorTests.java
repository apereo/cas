package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIdTokenClaimCollectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OIDCAttributes")
class OidcIdTokenClaimCollectorTests extends AbstractOidcTests {
    @Test
    void verifyDefaultOperation() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        val collector = OidcIdTokenClaimCollector.defaultCollector();
        collector.collect(registeredService, claims, "claim1", List.of("value1"));
        collector.collect(registeredService, claims, "claim2", List.of("value1", "value2"));
        assertEquals("value1", claims.getStringClaimValue("claim1"));
        assertEquals(List.of("value1", "value2"), claims.getStringListClaimValue("claim2"));
    }

    @Test
    void verifyListing() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        val collector = OidcIdTokenClaimCollector.listableCollector();
        collector.collect(registeredService, claims, "claim1", List.of("value1"));
        collector.collect(registeredService, claims, "claim2", List.of("value1", "value2"));
        assertEquals(List.of("value1"), claims.getStringListClaimValue("claim1"));
        assertEquals(List.of("value1", "value2"), claims.getStringListClaimValue("claim2"));
    }
}
