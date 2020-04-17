package org.apereo.cas.oidc.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
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
public class OidcUserProfileViewRendererDefaultTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyOperation() throws Exception {
        val response = new MockHttpServletResponse();
        val context = new JEEContext(new MockHttpServletRequest(), response);
        val accessToken = getAccessToken();
        val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertNotNull(entity.getBody());
        val result = MAPPER.readValue(entity.getBody().toString(), Map.class);
        assertTrue(result.containsKey(OidcConstants.CLAIM_AUTH_TIME));
        assertTrue(result.containsKey(OidcConstants.CLAIM_SUB));
        assertTrue(result.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID));
        assertTrue(result.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_CLIENT_ID));
        assertTrue(result.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
        assertTrue(result.containsKey(CasProtocolConstants.PARAMETER_SERVICE));
        val attrs = (Map) result.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES);
        assertTrue(attrs.containsKey("email"));
        assertEquals("casuser@example.org", attrs.get("email"));
    }

    @Test
    public void verifyOperationOAuth() throws Exception {
        val clientId = UUID.randomUUID().toString();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(new MockHttpServletRequest(), response);
        val accessToken = getAccessToken(clientId);
        val service = getOAuthRegisteredService(clientId, "https://somthing.com");

        servicesManager.save(service);

        val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertNotNull(entity.getBody());
    }

    @Test
    public void verifyOperationSigned() throws Exception {
        val clientId = UUID.randomUUID().toString();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(new MockHttpServletRequest(), response);
        val accessToken = getAccessToken(clientId);
        val service = getOidcRegisteredService(clientId);
        service.setUserInfoEncryptedResponseEncoding(OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
        service.setUserInfoEncryptedResponseAlg("RSA-OAEP-256");
        service.setUserInfoSigningAlg("RS256");
        service.setSignIdToken(true);
        service.setEncryptIdToken(true);
        servicesManager.save(service);

        val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertNotNull(entity.getBody());
    }
}
