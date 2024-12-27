package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.token.JwtBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20JwtBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuthToken")
class OAuth20JwtBuilderTests extends AbstractOAuth20Tests {
    @Test
    void verifyJwt() throws Throwable {
        servicesManager.save(getRegisteredService("clientid-jwt", "secret-jwt"));
        val service = CoreAuthenticationTestUtils.getService("https://service.example.com");
        val request = JwtBuilder.JwtRequest.builder()
            .issueDate(new Date())
            .jwtId(service.getId())
            .serviceAudience(Set.of("clientid-jwt"))
            .subject("casuser")
            .issuer(casProperties.getServer().getPrefix())
            .build();
        val jwt = accessTokenJwtBuilder.build(request);
        assertNotNull(jwt);
    }

    @Test
    void verifyBadJwt() {
        assertThrows(IllegalArgumentException.class, () -> JwtBuilder.parse("badly-formatted-jwt"));
    }

    @Test
    void verifyJwtWithSubjectResolution() throws Throwable {
        val registeredService = getRegisteredService("clientid2-jwt", "secret2-jwt");
        servicesManager.save(registeredService);
        val service = CoreAuthenticationTestUtils.getService("https://service.example.com");
        val request = JwtBuilder.JwtRequest
            .builder()
            .issueDate(new Date())
            .jwtId(service.getId())
            .serviceAudience(Set.of("clientid2-jwt"))
            .subject("casuser")
            .issuer(casProperties.getServer().getPrefix())
            .resolveSubject(true)
            .registeredService(Optional.of(registeredService))
            .build();
        val jwt = accessTokenJwtBuilder.build(request);
        assertNotNull(jwt);
        val claims = accessTokenJwtBuilder.unpack(jwt);
        assertEquals("casuser", claims.getSubject());
        assertEquals("clientid2-jwt", claims.getAudience().getFirst());
        assertEquals("apereo-cas", claims.getClaim("givenName"));
        assertEquals("cas", claims.getClaim("uid"));

    }
}
