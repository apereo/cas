package org.apereo.cas.ticket.accesstoken;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.token.JwtBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
        assertNull(JwtBuilder.parse("badly-formatted-jwt"));
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
        assertEquals("clientid2-jwt", claims.getClaim("client_id"));

    }

    @Test
    void verifyAttributeReleasePolicyNoResolution() throws Throwable {
        val registeredService = getRegisteredService("clientid3-jwt", "secret3-jwt");
        registeredService.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of("email")));
        servicesManager.save(registeredService);
        val service = CoreAuthenticationTestUtils.getService("https://service.example.com");
        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put("email", List.of("cas@example.org"));
        attributes.put("phone", List.of("1234567890"));
        val request = JwtBuilder.JwtRequest
            .builder()
            .issueDate(new Date())
            .jwtId(service.getId())
            .serviceAudience(Set.of("clientid3-jwt"))
            .subject("casuser")
            .issuer(casProperties.getServer().getPrefix())
            .attributes(attributes)
            .registeredService(Optional.of(registeredService))
            .build();
        val jwt = accessTokenJwtBuilder.build(request);
        assertNotNull(jwt);
        val claims = accessTokenJwtBuilder.unpack(jwt);
        assertEquals("casuser", claims.getSubject());
        assertEquals("clientid3-jwt", claims.getAudience().getFirst());
        assertEquals("cas@example.org", claims.getClaim("email"));
        assertNull(claims.getClaim("phone"));
        assertEquals("clientid3-jwt", claims.getClaim("client_id"));
    }
}
