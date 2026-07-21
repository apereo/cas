package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.JsonUtils;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
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
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "unknown", List.of());
        assertEquals(0, claims.getClaimNames().size());
    }

    @Test
    void verifyUnknownDefinition() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "unknown", List.of("value1", "value2"));
        assertEquals(2, claims.getStringListClaimValue("unknown").size());
    }

    @Test
    void verifyUnknownDefinitionAsSingle() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "unknown", List.of("value1"));
        assertEquals("value1", claims.getStringClaimValue("unknown"));
    }

    @Test
    void verifyMultiValueAsList() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "mail", List.of("cas1@example.org", "cas2@example.org"));
        assertEquals(2, claims.getStringListClaimValue("mail").size());
    }

    @Test
    void verifyJsonValue() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        val map = Map.of("key1", "value1", "key2", List.of("v2", "v3"));
        val json = JsonUtils.render(map);
        oidcIdTokenClaimCollector.collect(registeredService, claims, "container", List.of(json));
        assertEquals(map, claims.getClaimValue("container", Map.class));
    }

    @Test
    void verifyStructuredClaim() {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "organization", List.of("example.org", "apereo.org"));
        assertNull(claims.getClaimValue("organization"));
        val value = claims.getClaimValueAsString("org");
        assertEquals("{apereo={cas={entity=[example.org, apereo.org]}}}", value);
    }

    @Test
    void verifyStructuredClaimByDefnName() {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "org.apereo.cas.entity", List.of("example.org", "apereo.org"));
        assertNull(claims.getClaimValue("organization"));
        val value = claims.getClaimValueAsString("org");
        assertEquals("{apereo={cas={entity=[example.org, apereo.org]}}}", value);
    }
    
    @Test
    void verifySingleValueAsList() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "mail", List.of("cas@example.org"));
        assertEquals(1, claims.getStringListClaimValue("mail").size());
    }

    @Test
    void verifySingleValueAsSingleValue() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, claims, "cn", List.of("casuser"));
        assertEquals("casuser", claims.getStringClaimValue("cn"));
    }

    @Test
    void verifyAssurance() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val originalClaims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(registeredService, originalClaims, "assurance", List.of("value1", "value2"));
        oidcIdTokenClaimCollector.collect(registeredService, originalClaims, "homeCountry", List.of("USA", "UK"));
        oidcIdTokenClaimCollector.collect(registeredService, originalClaims, "mail", List.of("cas@apereo.org"));
        oidcIdTokenClaimCollector.conclude(registeredService, originalClaims);
        
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
