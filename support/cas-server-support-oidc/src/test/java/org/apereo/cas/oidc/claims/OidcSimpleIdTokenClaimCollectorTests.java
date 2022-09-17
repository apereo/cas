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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcSimpleIdTokenClaimCollectorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/oidc-definitions.json")
public class OidcSimpleIdTokenClaimCollectorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(OidcIdTokenClaimCollector.BEAN_NAME)
    private OidcIdTokenClaimCollector oidcIdTokenClaimCollector;

    @Test
    public void verifyEmptyValue() {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "unknown", List.of());
        assertEquals(0, claims.getClaimNames().size());
    }

    @Test
    public void verifyUnknownDefinition() throws Exception {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "unknown", List.of("value1", "value2"));
        assertEquals(2, claims.getStringListClaimValue("unknown").size());
    }

    @Test
    public void verifyUnknownDefinitionAsSingle() throws Exception {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "unknown", List.of("value1"));
        assertEquals("value1", claims.getStringClaimValue("unknown"));
    }

    @Test
    public void verifyMultiValueAsList() throws Exception {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "mail", List.of("cas1@example.org", "cas2@example.org"));
        assertEquals(2, claims.getStringListClaimValue("mail").size());
    }


    @Test
    public void verifySingleValueAsList() throws Exception {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "mail", List.of("cas@example.org"));
        assertEquals(1, claims.getStringListClaimValue("mail").size());
    }

    @Test
    public void verifySingleValueAsSingleValue() throws Exception {
        val claims = new JwtClaims();
        oidcIdTokenClaimCollector.collect(claims, "cn", List.of("casuser"));
        assertEquals("casuser", claims.getStringClaimValue("cn"));
    }

}
