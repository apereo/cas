package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcGroovyIdTokenClaimCollectorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@TestPropertySource(properties = "cas.authn.oidc.id-token.collector-script.location=classpath:/GroovyIdTokenClaims.groovy")
@Tag("Groovy")
public class OidcGroovyIdTokenClaimCollectorTests extends AbstractOidcTests {
    @Test
    void verifyScript() throws Exception {
        val registeredService = getOidcRegisteredService();
        val claims = new JwtClaims();
        oidcConfigurationContext
            .getIdTokenClaimCollectors()
            .forEach(Unchecked.consumer(collector -> {
                collector.collect(registeredService, claims, "cn", List.of("casuser"));
                collector.conclude(registeredService, claims);
            }));
        assertEquals("CAS User", claims.getStringClaimValue("cn"));
        assertEquals("ApereoCAS", claims.getStringClaimValue("givenName"));
    }

}
