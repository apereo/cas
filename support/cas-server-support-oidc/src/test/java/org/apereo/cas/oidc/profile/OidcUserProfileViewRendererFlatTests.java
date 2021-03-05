package org.apereo.cas.oidc.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUserProfileViewRendererFlatTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties = {
    "cas.authn.oidc.discovery.user-info-signing-alg-values-supported=RS256",
    "cas.authn.oidc.discovery.user-info-encryption-alg-values-supported=RSA1_5,RSA-OAEP,RSA-OAEP-256,A128KW,A192KW,A256KW",
    "cas.authn.oauth.user-profile-view-type=FLAT"
})
public class OidcUserProfileViewRendererFlatTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

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
        assertTrue(result.containsKey(CasProtocolConstants.PARAMETER_SERVICE));
        assertTrue(result.containsKey("email"));
        assertEquals("casuser@example.org", result.get("email"));
    }

    @Test
    public void verifyFailsOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setUserInfoSigningAlg(AlgorithmIdentifiers.NONE);
        service.setUserInfoEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(service);

        val response = new MockHttpServletResponse();
        val context = new JEEContext(new MockHttpServletRequest(), response);
        val accessToken = getAccessToken(id);
        val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
        val entity = oidcUserProfileViewRenderer.render(data, accessToken, response);
        assertNotNull(entity);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        service.setUserInfoSigningAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
        service.setUserInfoEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(service);
        assertEquals(HttpStatus.BAD_REQUEST, oidcUserProfileViewRenderer.render(data, accessToken, response).getStatusCode());
    }
}
