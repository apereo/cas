package org.apereo.cas.oidc.web.controllers.profile;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUserProfileEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcUserProfileEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcProfileController")
    protected OidcUserProfileEndpointController oidcUserProfileEndpointController;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    protected OAuth20AccessTokenFactory accessTokenFactory;

    @Test
    public void verify() throws Exception {
        val map = new HashMap<String, List<Object>>();
        map.put("cn", List.of("cas"));

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", map);
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(), null, new HashMap<>());
        ticketRegistry.addTicket(accessToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(),
            OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, accessToken.getId());
        val mockResponse = new MockHttpServletResponse();

        var entity = oidcUserProfileEndpointController.handleGetRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        entity = oidcUserProfileEndpointController.handlePostRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }
}
