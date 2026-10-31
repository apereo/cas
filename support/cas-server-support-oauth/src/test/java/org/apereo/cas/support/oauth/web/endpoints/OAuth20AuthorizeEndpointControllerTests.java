package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link OAuth20AuthorizeEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("OAuthWeb")
class OAuth20AuthorizeEndpointControllerTests extends AbstractOAuth20Tests {
    private static final String SERVICE_NAME = "serviceName";

    private static final String STATE = "state";

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Test
    void verifyNoClientId() throws Throwable {
        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val modelAndView = getModelAndView(performAuthorizeRequest(mockRequest));
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    void verifyNoRedirectUri() throws Throwable {
        val registeredService = addRegisteredService();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val modelAndView = getModelAndView(performAuthorizeRequest(mockRequest));
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    void verifyNoResponseType() throws Throwable {
        val registeredService = addRegisteredService();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);

        val result = performAuthorizeFlow(mockRequest);
        val modelAndView = getModelAndView(result);
        val redirectUrl = getRedirectUrl(result);
        assertNotNull(redirectUrl);
        assertRedirectBaseUrl(redirectUrl, REDIRECT_URI);

        assertTrue(modelAndView.getModel().containsKey(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE, modelAndView.getModel().get(OAuth20Constants.ERROR).toString());
    }

    @Test
    void verifyBadResponseType() throws Throwable {
        val registeredService = addRegisteredService();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, "badvalue");
        val result = performAuthorizeFlow(mockRequest);
        val modelAndView = getModelAndView(result);
        val redirectUrl = getRedirectUrl(result);
        assertNotNull(redirectUrl);
        assertRedirectBaseUrl(redirectUrl, REDIRECT_URI);

        assertTrue(modelAndView.getModel().containsKey(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE, modelAndView.getModel().get(OAuth20Constants.ERROR).toString());
    }

    @Test
    void verifyNoCasService() throws Throwable {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, UUID.randomUUID().toString());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val modelAndView = getModelAndView(performAuthorizeRequest(mockRequest));
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    void verifyLoginHandshake() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val authorizeUrl = "%s://%s/cas%s/%s?client_id=%s&redirect_uri=%s&response_type=code&state=%s".formatted(
            CAS_SCHEME, CAS_SERVER, OAuth20Constants.BASE_OAUTH20_URL, OAuth20Constants.AUTHORIZE_URL,
            service.getClientId(), URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8), STATE);
        val cookies = new LinkedHashMap<String, Cookie>();
        var result = performWithCookies(authorizeUrl, cookies);
        assertRedirectsToCasLoginForAuthorize(result);
        val serviceUrl = getRedirectParameterValue(result.getResponse().getRedirectedUrl(), CasProtocolConstants.PARAMETER_SERVICE);
        assertNotNull(serviceUrl);
        assertTrue(serviceUrl.endsWith("&client_name=" + Authenticators.CAS_OAUTH_CLIENT));

        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(ticketGrantingTicket);
        val serviceTicket = ticketGrantingTicket.grantServiceTicket(serviceFactory.createService(serviceUrl), mock(TicketTrackingPolicy.class));
        ticketRegistry.addTicket(serviceTicket);
        ticketRegistry.updateTicket(ticketGrantingTicket);

        result = performWithCookies(serviceUrl + '&' + CasProtocolConstants.PARAMETER_TICKET + '=' + serviceTicket.getId(), cookies);
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals(authorizeUrl, result.getResponse().getRedirectedUrl());

        val cookieRequest = new MockHttpServletRequest();
        cookieRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        ClientInfoHolder.setClientInfo(ClientInfo.from(cookieRequest));
        val cookieResponse = new MockHttpServletResponse();
        ticketGrantingTicketCookieGenerator.addCookie(cookieRequest, cookieResponse, ticketGrantingTicket.getId());
        Arrays.stream(cookieResponse.getCookies()).forEach(cookie -> cookies.put(cookie.getName(), cookie));
        result = performWithCookies(authorizeUrl, cookies);
        val redirectUrl = getRedirectUrl(result);
        assertNotNull(redirectUrl);
        assertRedirectBaseUrl(redirectUrl, REDIRECT_URI);
        assertNotNull(getRedirectParameterValue(redirectUrl, OAuth20Constants.CODE));
        assertEquals(STATE, getRedirectParameterValue(redirectUrl, OAuth20Constants.STATE));

        cookies.remove(ticketGrantingTicketCookieGenerator.getCookieName());
        assertRedirectsToCasLoginForAuthorize(performWithCookies(authorizeUrl, cookies));
    }

    @Test
    void verifyLoginHandshakeResumesPostAsGet() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        servicesManager.save(service);

        val authorizeUrl = "%s://%s/cas%s/%s".formatted(CAS_SCHEME, CAS_SERVER,
            OAuth20Constants.BASE_OAUTH20_URL, OAuth20Constants.AUTHORIZE_URL);
        val result = mockMvc.perform(MockMvcRequestBuilders.post(URI.create(authorizeUrl))
            .contextPath("/cas")
            .with(OAuth20AuthorizeEndpointControllerTests::withCasPort)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param(OAuth20Constants.CLIENT_ID, service.getClientId())
            .param(OAuth20Constants.REDIRECT_URI, REDIRECT_URI)
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType())).andReturn();
        assertRedirectsToCasLoginForAuthorize(result);
        val requestedUrl = oauthDistributedSessionStore.get(result.getRequest(), OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL).orElseThrow().toString();
        assertRedirectBaseUrl(requestedUrl, authorizeUrl);
        assertEquals(service.getClientId(), getRedirectParameterValue(requestedUrl, OAuth20Constants.CLIENT_ID));
        assertEquals(REDIRECT_URI, getRedirectParameterValue(requestedUrl, OAuth20Constants.REDIRECT_URI));
        assertEquals(OAuth20ResponseTypes.CODE.getType(), getRedirectParameterValue(requestedUrl, OAuth20Constants.RESPONSE_TYPE));
    }

    @Test
    void verifyAjaxRequestWithoutLoginIsRefused() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        mockRequest.addHeader("X-Requested-With", "XMLHttpRequest");
        val result = performAuthorizeRequest(mockRequest);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyRedirectUriDoesNotStartWithServiceId() throws Throwable {
        val registeredService = getOAuthRegisteredService(OTHER_REDIRECT_URI, UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val modelAndView = getModelAndView(performAuthorizeRequest(mockRequest));
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    void verifyCodeNoProfile() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);

        val session = new MockHttpSession();
        mockRequest.setSession(session);

        val result = performAuthorizeFlow(mockRequest);
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertNotNull(result.getResponse().getRedirectedUrl());
        assertTrue(result.getResponse().getRedirectedUrl().contains("/cas/login"));
    }

    @Test
    void verifyMissingTicketGrantingTicket() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        configurationContext.getServicesManager().save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val profile = new DefaultOAuth20UserProfile(ID, OAuth20UserProfile.BASIC_PROFILE_HANDLER_NAME);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile, null);

        val result = performAuthorizeRequest(mockRequest);
        assertRedirectsToCasLoginForAuthorize(result);
    }

    @Test
    void verifyServiceAccessDenied() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(Map.of("required", Set.of("value1"))));
        configurationContext.getServicesManager().save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val profile = new DefaultOAuth20UserProfile(ID, OAuth20UserProfile.BASIC_PROFILE_HANDLER_NAME);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val ticket = new MockTicketGrantingTicket("casuser");
        configurationContext.getTicketRegistry().addTicket(ticket);
        seedAuthorizeRequest(mockRequest, mockResponse, profile, ticket.getId());

        val result = performAuthorizeRequest(mockRequest);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, getModelAndView(result).getViewName());
    }

    @Test
    void verifyCodeRedirectToClient() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        configurationContext.getServicesManager().save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val properties = configurationContext.getCasProperties();
        properties.getAuthn().getOauth().getSessionReplication().getCookie().setAutoConfigureCookiePath(true);
        properties.getAuthn().getOauth().getSessionReplication().setReplicateSessions(true);
        configurationContext.getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

        val profile = buildCasProfile();
        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile);

        val result = performAuthorizeRequest(mockRequest);
        assertCodeIssuedForProfile(result, null);
    }

    @Test
    void verifyTokenRedirectToClient() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();
        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile);

        val result = performAuthorizeRequest(mockRequest);
        val accessToken = assertAccessTokenIssuedForProfile(result, null);
        val redirectUrl = Objects.requireNonNull(getRedirectUrl(result));
        assertEquals(getDefaultAccessTokenExpiration(), Long.parseLong(getRedirectParameterValue(redirectUrl, OAuth20Constants.EXPIRES_IN)));
        assertNotNull(accessToken);
    }

    @Test
    void verifyPerServiceExpiration() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        val expirationPolicy = new DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy();
        expirationPolicy.setMaxTimeToLive("5005");
        expirationPolicy.setTimeToKill("1001");
        service.setAccessTokenExpirationPolicy(expirationPolicy);
        service.setJwtAccessToken(true);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();
        val session = new MockHttpSession();
        mockRequest.setSession(session);

        seedAuthorizeRequest(mockRequest, mockResponse, profile);

        val result = performAuthorizeRequest(mockRequest);
        val redirectUrl = Objects.requireNonNull(getRedirectUrl(result));
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));
        val jwt = JwtClaims.parse(oauthAccessTokenJwtCipherExecutor.decode(getRedirectParameterValue(redirectUrl, OAuth20Constants.ACCESS_TOKEN)).toString());
        val lifetime = jwt.getExpirationTime().getValue() - jwt.getIssuedAt().getValue();
        assertTrue(Math.abs(Long.parseLong(expirationPolicy.getMaxTimeToLive()) - lifetime) <= DELTA);
        assertEquals(expirationPolicy.getMaxTimeToLive(), getRedirectParameterValue(redirectUrl, OAuth20Constants.EXPIRES_IN));
    }

    @Test
    void verifyAuthorizationRequestIsBoundToDPoPKey() throws Throwable {
        val controller = new OAuth20AuthorizeEndpointController<>(configurationContext);
        val authentication = RegisteredServiceTestUtils.getAuthentication();
        val request = new MockHttpServletRequest();
        assertFalse(controller.bindProofOfPossessionKey(request, authentication).containsAttribute(OAuth20Constants.DPOP_JKT));
        request.setParameter(OAuth20Constants.DPOP_JKT, "requested-thumbprint");
        assertEquals(List.of("requested-thumbprint"),
            controller.bindProofOfPossessionKey(request, authentication).getAttributes().get(OAuth20Constants.DPOP_JKT));
        request.setAttribute(OAuth20Constants.DPOP_JKT, "proven-thumbprint");
        assertEquals(List.of("proven-thumbprint"),
            controller.bindProofOfPossessionKey(request, authentication).getAttributes().get(OAuth20Constants.DPOP_JKT));
    }

    @Test
    void verifyCodeRedirectToClientWithState() throws Throwable {


        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuth20Constants.STATE, STATE);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();
        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile);

        val result = performAuthorizeRequest(mockRequest);
        assertCodeIssuedForProfile(result, STATE);
    }

    @Test
    void verifyTokenRedirectToClientWithState() throws Throwable {


        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuth20Constants.STATE, STATE);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();
        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile);

        val result = performAuthorizeRequest(mockRequest);
        assertAccessTokenIssuedForProfile(result, STATE);
    }

    @Test
    void verifyCodeRedirectToClientApproved() throws Throwable {


        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();
        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile);

        val result = performAuthorizeRequest(mockRequest);
        approve(mockRequest, result);
        assertCodeIssuedForProfile(performAuthorizeRequest(mockRequest), null);
    }

    @Test
    void verifyTokenRedirectToClientApproved() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile);

        val result = performAuthorizeRequest(mockRequest);
        approve(mockRequest, result);
        assertAccessTokenIssuedForProfile(performAuthorizeRequest(mockRequest), null);
    }

    @Test
    void verifyRedirectToApproval() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        seedAuthorizeRequest(mockRequest, mockResponse, profile);
        val result = performAuthorizeRequest(mockRequest);
        assertEquals(OAuth20Constants.CONFIRM_VIEW, getModelAndView(result).getViewName());
        assertNotNull(getModelAndView(result).getModel().get("approvalKey"));
    }

    @Test
    void verifyTokenRedirectToClientApprovedWithJwtToken() throws Throwable {


        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        service.setJwtAccessToken(true);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        configureAuthorizeSessionReplication();

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val ticket = new MockTicketGrantingTicket("casuser");
        configurationContext.getTicketRegistry().addTicket(ticket);
        seedAuthorizeRequest(mockRequest, mockResponse, profile, ticket.getId());
        configurationContext.getSessionStore().set(mockRequest, mockResponse, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");
        mockRequest.setCookies(mockResponse.getCookies());

        val result = performAuthorizeRequest(mockRequest);
        val redirectUrl = Objects.requireNonNull(getRedirectUrl(result));
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));
        val token = getRedirectParameterValue(redirectUrl, OAuth20Constants.ACCESS_TOKEN);
        assertNull(ticketRegistry.getTicket(token));
        val jwt = JwtClaims.parse(oauthAccessTokenJwtCipherExecutor.decode(token).toString());
        val accessToken = ticketRegistry.getTicket(jwt.getJwtId(), OAuth20AccessToken.class);
        assertNotNull(accessToken);
        assertProfilePrincipal(accessToken.getAuthentication().getPrincipal());
    }

    protected OAuth20UserProfile buildCasProfile() throws Throwable {
        val profile = new DefaultOAuth20UserProfile(ID, OAuth20UserProfile.BASIC_PROFILE_HANDLER_NAME);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        attributes.put(Authentication.class.getName(), CoreAuthenticationTestUtils.getAuthentication());

        val ticket = new MockTicketGrantingTicket("casuser");
        configurationContext.getTicketRegistry().addTicket(ticket);
        attributes.put(TicketGrantingTicket.class.getName(), ticket.getId());


        profile.addAttributes(attributes);
        return profile;
    }

    protected static OAuthRegisteredService getOAuthRegisteredService(final String serviceId, final String name) {
        val service = new OAuthRegisteredService();
        service.setName(name);
        service.setServiceId(serviceId);
        service.setClientId(UUID.randomUUID().toString());
        service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of(FIRST_NAME_ATTRIBUTE)));
        return service;
    }

    private void approve(final MockHttpServletRequest request, final MvcResult result) {
        val modelAndView = getModelAndView(result);
        assertEquals(OAuth20Constants.CONFIRM_VIEW, modelAndView.getViewName());
        request.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        request.addParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, modelAndView.getModel().get("approvalKey").toString());
    }

    private OAuth20Code assertCodeIssuedForProfile(final MvcResult result, final @Nullable String state) {
        val redirectUrl = getRedirectUrl(result);
        assertNotNull(redirectUrl);
        assertRedirectBaseUrl(redirectUrl, REDIRECT_URI);
        assertEquals(state, getRedirectParameterValue(redirectUrl, OAuth20Constants.STATE));
        val code = ticketRegistry.getTicket(getRedirectParameterValue(redirectUrl, OAuth20Constants.CODE), OAuth20Code.class);
        assertNotNull(code);
        assertProfilePrincipal(code.getAuthentication().getPrincipal());
        return code;
    }

    private OAuth20AccessToken assertAccessTokenIssuedForProfile(final MvcResult result, final @Nullable String state) {
        val redirectUrl = getRedirectUrl(result);
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));
        assertEquals(state, getRedirectParameterValue(redirectUrl, OAuth20Constants.STATE));
        val accessToken = ticketRegistry.getTicket(getRedirectParameterValue(redirectUrl, OAuth20Constants.ACCESS_TOKEN), OAuth20AccessToken.class);
        assertNotNull(accessToken);
        assertProfilePrincipal(accessToken.getAuthentication().getPrincipal());
        return accessToken;
    }

    private static void assertProfilePrincipal(final Principal principal) {
        assertEquals(ID, principal.getId());
        assertEquals(List.of(FIRST_NAME), principal.getAttributes().get(FIRST_NAME_ATTRIBUTE));
    }

    private static MockHttpServletRequest withCasPort(final MockHttpServletRequest request) {
        request.setServerPort(CAS_PORT);
        return request;
    }

    private MvcResult performWithCookies(final String url, final Map<String, Cookie> cookies) throws Exception {
        val builder = MockMvcRequestBuilders.get(URI.create(url))
            .contextPath("/cas")
            .header(HttpHeaders.USER_AGENT, "MSIE")
            .with(OAuth20AuthorizeEndpointControllerTests::withCasPort);
        if (!cookies.isEmpty()) {
            builder.cookie(cookies.values().toArray(Cookie[]::new));
        }
        val result = mockMvc.perform(builder).andReturn();
        Arrays.stream(result.getResponse().getCookies()).forEach(cookie -> cookies.put(cookie.getName(), cookie));
        return result;
    }

    private MvcResult performAuthorizeRequest(final MockHttpServletRequest request) throws Throwable {
        val result = performOAuthRequest(request);
        if (result.getResponse().getCookies().length > 0) {
            val cookies = new LinkedHashMap<String, Cookie>();
            Optional.ofNullable(request.getCookies()).stream().flatMap(Arrays::stream).forEach(cookie -> cookies.put(cookie.getName(), cookie));
            Arrays.stream(result.getResponse().getCookies()).forEach(cookie -> cookies.put(cookie.getName(), cookie));
            request.setCookies(cookies.values().toArray(Cookie[]::new));
        }
        return result;
    }

    private MvcResult performAuthorizeFlow(final MockHttpServletRequest request) throws Throwable {
        var currentRequest = request;
        var result = performAuthorizeRequest(currentRequest);
        for (var i = 0; i < 5; i++) {
            val redirectUrl = getRedirectUrl(result);
            if (!isInternalCasRedirect(redirectUrl)) {
                return result;
            }
            currentRequest = buildRedirectRequest(currentRequest, redirectUrl);
            result = performAuthorizeRequest(currentRequest);
        }
        return result;
    }

    private static @Nullable String getRedirectUrl(final MvcResult result) {
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

    private static @Nullable String getRedirectParameterValue(final @Nullable String redirectUrl, final String parameterName) {
        if (StringUtils.isBlank(redirectUrl)) {
            return null;
        }
        val uri = URI.create(redirectUrl);
        val queryValue = getParameterValue(uri.getRawQuery(), parameterName);
        if (StringUtils.isNotBlank(queryValue)) {
            return queryValue;
        }
        return getParameterValue(uri.getRawFragment(), parameterName);
    }

    private static @Nullable String getParameterValue(final @Nullable String data, final String parameterName) {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        return Arrays.stream(data.split("&"))
            .map(entry -> entry.split("=", 2))
            .filter(entry -> entry.length == 2)
            .filter(entry -> Objects.equals(entry[0], parameterName))
            .map(entry -> URLDecoder.decode(entry[1], StandardCharsets.UTF_8))
            .findFirst()
            .orElse(null);
    }

    private static boolean isInternalCasRedirect(final @Nullable String redirectUrl) {
        return StringUtils.isNotBlank(redirectUrl)
            && redirectUrl.startsWith(CAS_SCHEME + "://" + CAS_SERVER + ':' + CAS_PORT + "/cas/");
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
        val headerNames = originalRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement();
            request.addHeader(headerName, Collections.list(originalRequest.getHeaders(headerName)).toArray());
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

    private static void assertRedirectBaseUrl(final String redirectUrl, final String expectedBaseUrl) {
        val uri = URI.create(redirectUrl);
        val actualBaseUrl = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
        assertEquals(expectedBaseUrl, actualBaseUrl);
    }

    private static void assertRedirectsToCasLoginForAuthorize(final MvcResult result) {
        val redirectUrl = getRedirectUrl(result);
        assertNotNull(redirectUrl);
        val loginUri = URI.create(redirectUrl);
        assertEquals("/cas/login", loginUri.getPath());

        val service = getRedirectParameterValue(redirectUrl, "service");
        assertNotNull(service);
        val serviceUri = URI.create(service);
        assertEquals("/cas" + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL, serviceUri.getPath());
    }

    private void seedAuthorizeRequest(final MockHttpServletRequest request,
                                      final MockHttpServletResponse response,
                                      final OAuth20UserProfile profile) {
        val tgtId = profile.getAttributes().get(TicketGrantingTicket.class.getName());
        seedAuthorizeRequest(request, response, profile, tgtId != null ? tgtId.toString() : null);
    }

    private void seedAuthorizeRequest(final MockHttpServletRequest request,
                                      final MockHttpServletResponse response,
                                      final OAuth20UserProfile profile,
                                      final @Nullable String ticketGrantingTicketId) {
        Optional.ofNullable(request.getParameter(OAuth20Constants.CLIENT_ID))
            .ifPresent(clientId -> profile.addAttribute(OAuth20Constants.PROFILE_ATTRIBUTE_CLIENT_ID, clientId));
        oauthDistributedSessionStore.set(request, response, OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE, profile);
        if (StringUtils.isNotBlank(ticketGrantingTicketId)) {
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));
            val cookie = ticketGrantingTicketCookieGenerator.addCookie(request, response, ticketGrantingTicketId);
            assertNotNull(cookie);
        }
        if (response.getCookies() != null && response.getCookies().length > 0) {
            request.setCookies(response.getCookies());
        }
    }

    private void configureAuthorizeSessionReplication() {
        val properties = configurationContext.getCasProperties();
        properties.getAuthn().getOauth().getSessionReplication().getCookie().setAutoConfigureCookiePath(true);
        properties.getAuthn().getOauth().getSessionReplication().setReplicateSessions(true);
        configurationContext.getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);
    }

}
