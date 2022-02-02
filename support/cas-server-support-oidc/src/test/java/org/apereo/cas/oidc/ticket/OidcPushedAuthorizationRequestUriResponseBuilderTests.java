package org.apereo.cas.oidc.ticket;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPushedAuthorizationRequestUriResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
public class OidcPushedAuthorizationRequestUriResponseBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcPushedAuthorizationRequestResponseBuilder")
    private OAuth20AuthorizationResponseBuilder oidcPushedAuthorizationRequestResponseBuilder;

    @Test
    public void verifyOperation() throws Exception {
        assertEquals(0, oidcPushedAuthorizationRequestResponseBuilder.getOrder());

        val registeredService = getOidcRegisteredService();
        val profile = new CommonProfile();
        profile.setId("casuser");

        val request = getHttpRequestForEndpoint(OidcConstants.PUSHED_AUTHORIZE_URL);
        request.addParameter("param1", "value1");
        request.addParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL2);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);

        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .userProfile(profile)
            .build();

        val authzRequest = OAuth20AuthorizationRequest.builder()
            .responseType(OAuth20ResponseTypes.ID_TOKEN.getType())
            .build();
        
        assertTrue(oidcPushedAuthorizationRequestResponseBuilder.supports(authzRequest));
        val mv = oidcPushedAuthorizationRequestResponseBuilder.build(holder);
        assertTrue(mv.getModel().containsKey(OidcConstants.EXPIRES_IN));
        val uri = mv.getModel().get(OidcConstants.REQUEST_URI).toString();
        val ticket = ticketRegistry.getTicket(uri, OidcPushedAuthorizationRequest.class);
        assertNotNull(ticket);
    }
}
