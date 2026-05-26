package org.apereo.cas.oidc.web.controllers.authorize;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20ClientIdAwareProfileManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
            val result = mockMvc.perform(get("/cas/oidc/" + OidcConstants.AUTHORIZE_URL)
                    .param(OAuth20Constants.CLIENT_ID, id)
                    .with(withHttpRequestProcessor()))
                .andReturn();
            assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
        }

    }

    @Nested
    class DefaultAuthorizationRequests extends AbstractOidcTests {
        @Test
        void verifyBadEndpointRequest() throws Exception {
            val result = mockMvc.perform(get("/cas/unknown/" + OidcConstants.AUTHORIZE_URL)
                    .with(withHttpRequestProcessor()))
                .andReturn();
            assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
        }

        @Test
        void verifyUnknownPrompt() throws Exception {
            val id = UUID.randomUUID().toString();
            val service = getOidcRegisteredService(id);
            service.setBypassApprovalPrompt(true);
            servicesManager.save(service);

            val result = mockMvc.perform(get("/cas/oidc/" + OidcConstants.AUTHORIZE_URL)
                    .param(OAuth20Constants.CLIENT_ID, id)
                    .param(OAuth20Constants.PROMPT, "unknown")
                    .with(withHttpRequestProcessor()))
                .andReturn();
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
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
            profile.setClientName(Authenticators.CAS_OAUTH_CLIENT);

            val sessionStore = oauthContext.getSessionStore();
            val context = new JEEContext(mockRequest, mockResponse);
            val ticket = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(ticket);
            profile.addAttribute(TicketGrantingTicket.class.getName(), ticket.getId());
            seedAuthorizeRequest(mockRequest, mockResponse, profile, ticket.getId());
            sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val result = performAuthorizeFlow(mockRequest, mockResponse);
            assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
            assertNull(getRedirectUrl(result));
        }

        private MvcResult performRequest(final MockHttpServletRequest request,
                                         final MockHttpServletResponse response) throws Exception {
            val builder = MockMvcRequestBuilders
                .request(HttpMethod.valueOf(request.getMethod()), request.getRequestURI())
                .with(mockRequest -> {
                    mockRequest.setScheme(request.getScheme());
                    mockRequest.setServerName(request.getServerName());
                    mockRequest.setServerPort(request.getServerPort());
                    mockRequest.setContextPath(request.getContextPath());
                    return mockRequest;
                });
            val parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                val parameterName = parameterNames.nextElement();
                val parameterValues = request.getParameterValues(parameterName);
                if (parameterValues != null) {
                    builder.param(parameterName, parameterValues);
                }
            }
            if (request.getSession(false) instanceof final MockHttpSession session) {
                builder.session(session);
            }
            if (request.getCookies() != null) {
                builder.cookie(request.getCookies());
            }
            if (response.getCookies().length > 0) {
                builder.cookie(response.getCookies());
            }
            return mockMvc.perform(builder).andReturn();
        }

        private MvcResult performAuthorizeFlow(final MockHttpServletRequest request,
                                               final MockHttpServletResponse response) throws Exception {
            var currentRequest = request;
            var currentResponse = response;
            var result = performRequest(currentRequest, currentResponse);
            for (var i = 0; i < 5; i++) {
                val redirectUrl = getRedirectUrl(result);
                if (!isInternalCasRedirect(redirectUrl)) {
                    return result;
                }
                currentRequest = buildRedirectRequest(currentRequest, redirectUrl);
                currentResponse = new MockHttpServletResponse();
                result = performRequest(currentRequest, currentResponse);
            }
            return result;
        }

        private void seedAuthorizeRequest(final MockHttpServletRequest request,
                                          final MockHttpServletResponse response,
                                          final CasProfile profile,
                                          final String ticketGrantingTicketId) {
            val context = new JEEContext(request, response);
            val profiles = CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile);
            val profileManager = new OAuth20ClientIdAwareProfileManager(
                context,
                oauthDistributedSessionStore,
                servicesManager,
                oidcConfigurationContext.getRequestParameterResolver());
            profileManager.save(true, profile, false);
            oauthDistributedSessionStore.set(context, Pac4jConstants.USER_PROFILES, profiles);
            oidcConfigurationContext.getSessionStore().set(context, Pac4jConstants.USER_PROFILES, profiles);
            val cookie = ticketGrantingTicketCookieGenerator.addCookie(request, response, ticketGrantingTicketId);
            assertNotNull(cookie);
            if (response.getCookies().length > 0) {
                request.setCookies(response.getCookies());
            }
        }

        private static String getRedirectUrl(final MvcResult result) {
            val redirectedUrl = result.getResponse().getRedirectedUrl();
            if (StringUtils.isNotBlank(redirectedUrl)) {
                return redirectedUrl;
            }
            val modelAndView = result.getModelAndView();
            if (modelAndView != null) {
                if (modelAndView.getView() instanceof final RedirectView redirectView) {
                    return redirectView.getUrl();
                }
                if (modelAndView.getViewName() != null && modelAndView.getViewName().startsWith("redirect:")) {
                    return modelAndView.getViewName().substring("redirect:".length());
                }
            }
            return null;
        }

        private static boolean isInternalCasRedirect(final String redirectUrl) {
            return StringUtils.isNotBlank(redirectUrl)
                && redirectUrl.startsWith("https://sso.example.org/cas/");
        }

        private static MockHttpServletRequest buildRedirectRequest(final MockHttpServletRequest originalRequest,
                                                                   final String redirectUrl) {
            val uri = URI.create(redirectUrl);
            val request = new MockHttpServletRequest(HttpMethod.GET.name(), uri.getPath());
            request.setScheme(uri.getScheme());
            request.setServerName(uri.getHost());
            request.setServerPort(uri.getPort());
            request.setContextPath("/cas");
            if (originalRequest.getSession(false) instanceof final MockHttpSession session) {
                request.setSession(session);
            }
            if (originalRequest.getCookies() != null) {
                request.setCookies(originalRequest.getCookies());
            }
            val query = uri.getRawQuery();
            if (StringUtils.isNotBlank(query)) {
                Arrays.stream(query.split("&"))
                    .map(entry -> entry.split("=", 2))
                    .filter(entry -> entry.length == 2)
                    .forEach(entry -> request.addParameter(
                        URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(entry[1], StandardCharsets.UTF_8)));
            }
            return request;
        }
    }

}
