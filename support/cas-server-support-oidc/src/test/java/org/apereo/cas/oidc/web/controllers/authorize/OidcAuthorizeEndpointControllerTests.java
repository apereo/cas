package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.RedirectView;
import java.util.Locale;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDCWeb")
class OidcAuthorizeEndpointControllerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.discovery.require-pushed-authorization-requests=true")
    class PushedAuthorizationRequests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcAuthorizeController")
        protected OidcAuthorizeEndpointController oidcAuthorizeEndpointController;

        @Test
        void verifyBadEndpointRequest() throws Throwable {
            val id = UUID.randomUUID().toString();
            val request = getHttpRequestForEndpoint(OidcConstants.AUTHORIZE_URL);
            request.setMethod(HttpMethod.GET.name());
            request.setParameter(OAuth20Constants.CLIENT_ID, id);
            val response = new MockHttpServletResponse();
            val mv = oidcAuthorizeEndpointController.handleRequest(request, response);
            assertEquals(HttpStatus.FORBIDDEN, mv.getStatus());
        }

    }

    @Nested
    class DefaultAuthorizationRequests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcAuthorizeController")
        protected OidcAuthorizeEndpointController oidcAuthorizeEndpointController;

        @Test
        void verifyBadEndpointRequest() throws Throwable {
            val request = getHttpRequestForEndpoint("unknown/issuer");
            request.setRequestURI("unknown/issuer");
            val response = new MockHttpServletResponse();
            val mv = oidcAuthorizeEndpointController.handleRequest(request, response);
            assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
        }

        @Test
        void verifyUnknownPrompt() throws Throwable {
            val id = UUID.randomUUID().toString();
            val service = getOidcRegisteredService(id);
            service.setBypassApprovalPrompt(true);
            servicesManager.save(service);

            val mockRequest = getHttpRequestForEndpoint(OidcConstants.AUTHORIZE_URL);
            mockRequest.setMethod(HttpMethod.GET.name());
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, id);
            mockRequest.setParameter(OAuth20Constants.PROMPT, "unknown");
            mockRequest.setContextPath(StringUtils.EMPTY);
            mockRequest.setQueryString(OAuth20Constants.PROMPT + "=unknown");
            val mockResponse = new MockHttpServletResponse();
            val mv = oidcAuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
        }

        @Test
        void verify() throws Throwable {
            val id = UUID.randomUUID().toString();
            val mockRequest = getHttpRequestForEndpoint(OidcConstants.AUTHORIZE_URL);
            mockRequest.setMethod(HttpMethod.GET.name());
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, id);
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/");
            mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setContextPath(StringUtils.EMPTY);
            val mockResponse = new MockHttpServletResponse();

            val oauthContext = oidcAuthorizeEndpointController.getConfigurationContext();
            oauthContext.getCasProperties().getAuthn().getOauth().getSessionReplication().getCookie().setAutoConfigureCookiePath(false);
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
            profile.addAttribute(TicketGrantingTicket.class.getName(), ticket.getId());
            sessionStore.set(context, Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val modelAndView = oidcAuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
            val view = modelAndView.getView();
            assertInstanceOf(RedirectView.class, view);
            val url = ((AbstractUrlBasedView) view).getUrl();
            assertTrue(url.startsWith("https://oauth.example.org/"));

            val fragment = new URIBuilder(url).getFragment();
            assertTrue(fragment.contains(OAuth20Constants.ACCESS_TOKEN));
            assertTrue(fragment.contains(OAuth20Constants.EXPIRES_IN));
            assertTrue(fragment.contains(OAuth20Constants.TOKEN_TYPE));
        }
    }

}
