package org.apereo.cas.oidc.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUserProfileViewRendererDefaultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
class OidcUserProfileViewRendererDefaultTests extends AbstractOidcTests {
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
        assertTrue(result.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
        assertTrue(result.containsKey(CasProtocolConstants.PARAMETER_SERVICE));
        val attrs = (Map) result.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES);
        assertTrue(attrs.containsKey("email"));
        assertEquals("casuser@example.org", CollectionUtils.firstElement(attrs.get("email")).get());
    }

    @Test
    void verifyOperationOAuth() throws Throwable {
        val clientId = UUID.randomUUID().toString();
        val response = new MockHttpServletResponse();
        val accessToken = getAccessToken(clientId);
        val service = getOAuthRegisteredService(clientId, "https://something.com");

        servicesManager.save(service);

        val data = oidcUserProfileDataCreator.createFrom(accessToken);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertNotNull(entity.getBody());
    }

    @Test
    void verifyOperationEncryptedAndSigned() throws Throwable {
        val clientId = UUID.randomUUID().toString();
        val response = new MockHttpServletResponse();
        val accessToken = getAccessToken(clientId);
        val service = getOidcRegisteredService(clientId);
        service.setUserInfoEncryptedResponseEncoding(OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
        service.setUserInfoEncryptedResponseAlg("RSA-OAEP-256");
        service.setUserInfoSigningAlg("RS256");
        service.setSignIdToken(true);
        service.setEncryptIdToken(true);
        servicesManager.save(service);

        val data = oidcUserProfileDataCreator.createFrom(accessToken);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertNotNull(entity.getBody());
    }

    @Test
    void verifyOperationSigned() throws Throwable {
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
        assertEquals("casuser@example.org", ((Map<String, Object>) claims.getClaim("attributes")).get("email"));
        assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
    }
}
