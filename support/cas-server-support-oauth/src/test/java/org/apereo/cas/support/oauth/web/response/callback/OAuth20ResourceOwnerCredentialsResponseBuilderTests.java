package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ResourceOwnerCredentialsResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20ResourceOwnerCredentialsResponseBuilderTests extends AbstractOAuth20Tests {

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());

        val holder = AccessTokenRequestDataHolder.builder()
            .clientId(CLIENT_ID)
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication(
                CoreAuthenticationTestUtils.getPrincipal("casuser")))
            .registeredService(getRegisteredService(CLIENT_ID, CLIENT_SECRET))
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(context, CLIENT_ID, holder));
        oauthResourceOwnerCredentialsResponseBuilder.buildResponseModelAndView(context, servicesManager,
            CLIENT_ID, "https://example.org", Map.of());
    }

    @Test
    public void verifyModelAndViewPost() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());

        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.buildResponseModelAndView(context, servicesManager,
            CLIENT_ID, "https://example.org", Map.of("key", "value")));
    }

    @Test
    public void verifyModelAndView() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.buildResponseModelAndView(context, servicesManager,
            CLIENT_ID, "https://example.org", Map.of("key", "value")));
    }
}
