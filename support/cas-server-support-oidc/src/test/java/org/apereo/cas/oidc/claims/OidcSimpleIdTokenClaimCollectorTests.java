package org.apereo.cas.oidc.claims;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcSimpleIdTokenClaimCollectorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDCAttributes")
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/oidc-definitions.json",
    "cas.authn.oidc.identity-assurance.verification-source.location=classpath:assurance/id-1.json"
})
class OidcSimpleIdTokenClaimCollectorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(OidcIdTokenClaimCollector.BEAN_NAME)
    private OidcIdTokenClaimCollector oidcIdTokenClaimCollector;

    @Test
    void verifyEmptyValue() {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "unknown", List.of());
        assertEquals(0, claims.getClaimNames().size());
    }

    @Test
    void verifyUnknownDefinition() throws Throwable {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "unknown", List.of("value1", "value2"));
        assertEquals(2, claims.getStringListClaimValue("unknown").size());
    }

    @Test
    void verifyUnknownDefinitionAsSingle() throws Throwable {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "unknown", List.of("value1"));
        assertEquals("value1", claims.getStringClaimValue("unknown"));
    }

    @Test
    void verifyMultiValueAsList() throws Throwable {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "mail", List.of("cas1@example.org", "cas2@example.org"));
        assertEquals(2, claims.getStringListClaimValue("mail").size());
    }

    @Test
    void verifyStructuredClaim() {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "organization", List.of("example.org", "apereo.org"));
        assertNull(claims.getClaimValue("organization"));
        val value = claims.getClaimValueAsString("org");
        assertEquals("{apereo={cas={entity=[example.org, apereo.org]}}}", value);
    }

    @Test
    void verifyStructuredClaimByDefnName() {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "org.apereo.cas.entity", List.of("example.org", "apereo.org"));
        assertNull(claims.getClaimValue("organization"));
        val value = claims.getClaimValueAsString("org");
        assertEquals("{apereo={cas={entity=[example.org, apereo.org]}}}", value);
    }
    
    @Test
    void verifySingleValueAsList() throws Throwable {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "mail", List.of("cas@example.org"));
        assertEquals(1, claims.getStringListClaimValue("mail").size());
    }

    @Test
    void verifySingleValueAsSingleValue() throws Throwable {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "cn", List.of("casuser"));
        assertEquals("casuser", claims.getStringClaimValue("cn"));
    }

    @Test
    void verifyAssurance() throws Throwable {
        val originalClaims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(originalClaims, "assurance", List.of("value1", "value2"));
        oidcIdTokenClaimCollector.collect(originalClaims, "homeCountry", List.of("USA", "UK"));
        oidcIdTokenClaimCollector.collect(originalClaims, "mail", List.of("cas@apereo.org"));
        oidcIdTokenClaimCollector.conclude(originalClaims);
        
        assertFalse(originalClaims.hasClaim("assurance"));
        assertFalse(originalClaims.hasClaim("home-country"));
        assertFalse(originalClaims.hasClaim("homeCountry"));
        assertTrue(originalClaims.hasClaim("mail"));

        val verifiedClaims = originalClaims.getClaimValue("verified_claims", Map.class);
        assertTrue(verifiedClaims.containsKey("verification"));
        assertTrue(verifiedClaims.containsKey("claims"));

        val claims = (Map) verifiedClaims.get("claims");
        assertTrue(claims.containsKey("assurance"));
        assertTrue(claims.containsKey("home-country"));
        assertFalse(claims.containsKey("mail"));
    }
}
