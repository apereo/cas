package org.apereo.cas.uma.ticket.rpt;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaRequestingPartyTokenSigningServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class UmaRequestingPartyTokenSigningServiceTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyUnknownJwks() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getOauth().getUma().getCore().setIssuer("cas");
        val jwks = new ClassPathResource("nothing.jwks");
        props.getAuthn().getOauth().getUma().getRequestingPartyToken().getJwksFile().setLocation(jwks);
        val signingService = new UmaRequestingPartyTokenSigningService(props);
        assertThrows(IllegalArgumentException.class, () -> signingService.getJsonWebKeySigningKey(Optional.empty()));
        val service = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertEquals(JsonWebTokenSigner.ALGORITHM_ALL_EXCEPT_NONE, signingService.getAllowedSigningAlgorithms(service));
        assertThrows(IllegalArgumentException.class, () -> signingService.encode(service, new JwtClaims()));
    }

    /**
     * Requesting party tokens are signed with the keys of the JWKS file and never issued as unsigned JWTs.
     */
    @Test
    void verifyRequestingPartyTokenIsSigned() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getAuthn().getOauth().getUma().getCore().setIssuer("cas");
        props.getAuthn().getOauth().getUma().getRequestingPartyToken().getJwksFile().setLocation(new ClassPathResource("uma-keystore.jwks"));
        val signingService = new UmaRequestingPartyTokenSigningService(props);
        val service = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertTrue(signingService.shouldSignToken(service));
        val claims = new JwtClaims();
        claims.setSubject("casuser");
        claims.setIssuer("cas");
        claims.setClaim(OAuth20Constants.CLIENT_ID, service.getClientId());
        val rpt = signingService.encode(service, claims);
        assertFalse(JwtBuilder.isUnsigned(rpt));
        assertEquals("casuser", signingService.decode(rpt, Optional.of(service)).getSubject());
    }
}
