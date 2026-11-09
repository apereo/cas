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

    /**
     * {@code none} as the user info signing or encryption algorithm means no signing or no encryption: without either,
     * user info is plain JSON, and with signing alone it is a signed JWT; it is never an unsigned JWT.
     */
    @Test
    void verifyNoneMeansNoSigningOrEncryption() throws Throwable {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setUserInfoSigningAlg(AlgorithmIdentifiers.NONE);
        service.setUserInfoEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(service);

        val response = new MockHttpServletResponse();
        val accessToken = getAccessToken(id);
        val data = oidcUserProfileDataCreator.createFrom(accessToken);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("casuser@example.org", MAPPER.readValue(entity.getBody().toString(), Map.class).get("email"));

        service.setUserInfoSigningAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
        service.setUserInfoEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(service);
        val signed = oidcUserProfileViewRenderer.render(data, accessToken, new MockHttpServletResponse());
        assertEquals(HttpStatus.OK, signed.getStatusCode());
        val body = signed.getBody().toString();
        assertFalse(JwtBuilder.isUnsigned(body));
        assertEquals("casuser@example.org", Objects.requireNonNull(JwtBuilder.parse(body)).getClaim("email"));
    }
}
