package org.apereo.cas.oidc.web.controllers.authorize;

import module java.base;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.RedirectView;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
        @Test
        void verifyBadEndpointRequest() throws Exception {
            val id = UUID.randomUUID().toString();
            val mv = mockMvc.perform(get("/cas/oidc/" + OidcConstants.AUTHORIZE_URL)
                    .param(OAuth20Constants.CLIENT_ID, id)
                    .with(withHttpRequestProcessor()))
                .andReturn()
                .getModelAndView();
            assertNotNull(mv);
            assertEquals(HttpStatus.FORBIDDEN, mv.getStatus());
        }

    }

    @Nested
    class DefaultAuthorizationRequests extends AbstractOidcTests {
        @Test
        void verifyBadEndpointRequest() throws Exception {
            val mv = mockMvc.perform(get("/cas/unknown/" + OidcConstants.AUTHORIZE_URL)
                    .with(withHttpRequestProcessor()))
                .andReturn()
                .getModelAndView();
            assertNotNull(mv);
            assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
        }

        @Test
        void verifyUnknownPrompt() throws Exception {
            val id = UUID.randomUUID().toString();
            val service = getOidcRegisteredService(id);
            service.setBypassApprovalPrompt(true);
            servicesManager.save(service);

            val mv = mockMvc.perform(get("/cas/oidc/" + OidcConstants.AUTHORIZE_URL)
                    .param(OAuth20Constants.CLIENT_ID, id)
                    .param(OAuth20Constants.PROMPT, "unknown")
                    .with(withHttpRequestProcessor()))
                .andReturn()
                .getModelAndView();
            assertNotNull(mv);
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

            val oauthContext = oidcConfigurationContext;
            oauthContext.getCasProperties().getAuthn().getOauth().getSessionReplication().getCookie().setAutoConfigureCookiePath(false);
            oauthContext.getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

            val service = getOidcRegisteredService(id);
            service.setBypassApprovalPrompt(true);
            servicesManager.save(service);

            val profile = new CasProfile();
            profile.setId("casuser");

            val sessionStore = oauthContext.getSessionStore();
            val context = new JEEContext(mockRequest, mockResponse);
            val ticket = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(ticket);
            profile.addAttribute(TicketGrantingTicket.class.getName(), ticket.getId());
            sessionStore.set(context, Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val result = mockMvc.perform(prepareAuthorizeRequest(mockRequest, mockResponse)).andReturn();
            val modelAndView = result.getModelAndView();
            assertNotNull(modelAndView);
            val view = modelAndView.getView();
            assertInstanceOf(RedirectView.class, view);
            val url = ((AbstractUrlBasedView) view).getUrl();
            assertNotNull(url);
            assertTrue(url.startsWith("https://oauth.example.org/"));

            val fragment = new URIBuilder(url).getFragment();
            assertTrue(fragment.contains(OAuth20Constants.ACCESS_TOKEN));
            assertTrue(fragment.contains(OAuth20Constants.EXPIRES_IN));
            assertTrue(fragment.contains(OAuth20Constants.TOKEN_TYPE));
        }

        private MockHttpServletRequestBuilder prepareAuthorizeRequest(final MockHttpServletRequest request,
                                                                      final MockHttpServletResponse response) {
            val builder = get("/cas/oidc/" + OidcConstants.AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, request.getParameter(OAuth20Constants.CLIENT_ID))
                .param(OAuth20Constants.REDIRECT_URI, request.getParameter(OAuth20Constants.REDIRECT_URI))
                .param(OAuth20Constants.RESPONSE_TYPE, request.getParameter(OAuth20Constants.RESPONSE_TYPE))
                .with(withHttpRequestProcessor());
            if (request.getSession(false) instanceof final MockHttpSession session) {
                builder.session(session);
            }
            if (response.getCookies().length > 0) {
                builder.cookie(response.getCookies());
            }
            return builder;
        }
    }

}
