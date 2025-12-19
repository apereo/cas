package org.apereo.cas.oidc.profile;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUserProfileViewRendererFlatTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
@TestPropertySource(properties = {
    "cas.authn.oidc.discovery.user-info-signing-alg-values-supported=RS256",
    "cas.authn.oidc.discovery.user-info-encryption-alg-values-supported=RSA1_5,RSA-OAEP,RSA-OAEP-256,A128KW,A192KW,A256KW",
    "cas.authn.oauth.core.user-profile-view-type=FLAT"
})
class OidcUserProfileViewRendererFlatTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyOperation() throws Throwable {
        val response = new MockHttpServletResponse();
        val accessToken = getAccessToken();
        val data = oidcUserProfileDataCreator.createFrom(accessToken);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertNotNull(entity.getBody());
        val result = MAPPER.readValue(entity.getBody().toString(), Map.class);
        assertTrue(result.containsKey(OidcConstants.CLAIM_AUTH_TIME));
        assertTrue(result.containsKey(OAuth20Constants.CLAIM_SUB));
        assertTrue(result.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID));
        assertTrue(result.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_CLIENT_ID));
        assertTrue(result.containsKey(CasProtocolConstants.PARAMETER_SERVICE));
        assertTrue(result.containsKey("email"));
        val values = result.get("email").toString();
        assertEquals("casuser@example.org", values);
    }

    @Test
    void verifyOperationJWS() throws Throwable {
        val clientId = UUID.randomUUID().toString();
        val response = new MockHttpServletResponse();
        val accessToken = getAccessToken(clientId);
        val service = getOidcRegisteredService(clientId);
        service.setUserInfoSigningAlg("RS256");
        service.setSignIdToken(true);
        service.setEncryptIdToken(false);
        servicesManager.save(service);

        val data = oidcUserProfileDataCreator.createFrom(accessToken);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        val body = (String) entity.getBody();
        assertNotNull(body);
        val claims = JwtBuilder.parse(body);
        assertNotNull(claims);
        assertEquals("casuser@example.org", claims.getClaim("email"));
        assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setUserInfoSigningAlg(AlgorithmIdentifiers.NONE);
        service.setUserInfoEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(service);

        val response = new MockHttpServletResponse();
        val accessToken = getAccessToken(id);
        val data = oidcUserProfileDataCreator.createFrom(accessToken);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        service.setUserInfoSigningAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
        service.setUserInfoEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(service);
        assertEquals(HttpStatus.BAD_REQUEST, oidcUserProfileViewRenderer.render(data, accessToken, response).getStatusCode());
    }
}
