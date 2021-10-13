package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcAuthorizeEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAuthorizeController")
    protected OidcAuthorizeEndpointController oidcAuthorizeEndpointController;

    @Test
    public void verifyBadEndpointRequest() throws Exception {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = oidcAuthorizeEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.NOT_FOUND, mv.getStatus());
    }

    @Test
    public void verify() throws Exception {
        val id = UUID.randomUUID().toString();
        val mockRequest = getHttpRequestForEndpoint(OidcConstants.AUTHORIZE_URL);
        mockRequest.setMethod(HttpMethod.GET.name());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, id);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/");
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val oauthContext = oidcAuthorizeEndpointController.getConfigurationContext();
        oauthContext.getCasProperties().getSessionReplication().getCookie().setAutoConfigureCookiePath(false);
        oauthContext.getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId("casuser");

        val sessionStore = oidcAuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oidcAuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        var modelAndView = oidcAuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        var view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);

        modelAndView = oidcAuthorizeEndpointController.handleRequestPost(mockRequest, mockResponse);
        view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
    }

}
