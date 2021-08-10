package org.apereo.cas.oidc.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcUserProfileDataCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcUserProfileDataCreatorTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val accessToken = getAccessToken();
        val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
        assertFalse(data.isEmpty());
        assertEquals(accessToken.getTicketGrantingTicket().getAuthentication().getAuthenticationDate().toEpochSecond(), (long) data.get(OidcConstants.CLAIM_AUTH_TIME));
        assertTrue(data.containsKey(OidcConstants.CLAIM_SUB));
        assertTrue(data.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID));
        assertTrue(data.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_CLIENT_ID));
        assertTrue(data.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
        assertTrue(data.containsKey(CasProtocolConstants.PARAMETER_SERVICE));

        val attrs = (Map) data.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES);
        assertTrue(attrs.containsKey("email"));

    }

    @Test
    public void verifyTokenWithClaims() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val claims = "\"userinfo\": {\"given_name\": {\"essential\": true}}";
        request.addParameter(OAuth20Constants.CLAIMS, claims);
        val result = OAuth20Utils.parseRequestClaims(context);

        val accessToken = getAccessToken();
        when(accessToken.getClaims()).thenReturn(result);
        val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
        assertFalse(data.isEmpty());
        assertTrue(data.containsKey(OidcConstants.CLAIM_SUB));
    }
}
