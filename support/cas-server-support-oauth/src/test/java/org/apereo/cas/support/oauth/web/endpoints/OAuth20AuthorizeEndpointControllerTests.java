package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20ClientIdAwareProfileManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.view.RedirectView;
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
    void verifyCasClientCanValidate() throws Throwable {
        val service = getOAuthRegisteredService(REDIRECT_URI, SERVICE_NAME);
        servicesManager.save(service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        val mockResponse = new MockHttpServletResponse();
        val callContext = new CallContext(new JEEContext(mockRequest, mockResponse),
            oauthDistributedSessionStore, ProfileManagerFactory.DEFAULT);
        val redirect = oauthCasClient.getRedirectionAction(callContext);
        assertTrue(redirect.isPresent());

        val callbackUrl = ((WithLocationAction) redirect.get()).getLocation();
        val callback = RegisteredServiceTestUtils.getRegisteredService(callbackUrl);
        servicesManager.save(callback);

        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        ticketRegistry.addTicket(tgt);

        val trackingPolicy = mock(TicketTrackingPolicy.class);
        val ticketService = RegisteredServiceTestUtils.getService(REDIRECT_URI);
        ticketService.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(service.getClientId()));
        val st1 = tgt.grantServiceTicket(ticketService, trackingPolicy);

        ticketRegistry.addTicket(st1);
        ticketRegistry.updateTicket(tgt);
        mockRequest.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_TICKET, st1.getId());
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, callbackUrl);

        val clientCallContext = new CallContext(new JEEContext(mockRequest, mockResponse),
            oauthDistributedSessionStore, ProfileManagerFactory.DEFAULT);
        val result = oauthCasClient.getCredentials(clientCallContext)
            .map(credentials -> oauthCasClient.validateCredentials(clientCallContext, credentials))
            .orElseThrow();
        assertTrue(result.isPresent());
        assertNotNull(result.get().getUserProfile());
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

        val profile = new CasProfile();
        profile.setId(ID);
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        seedAuthorizeRequest(mockRequest, mockResponse, profile, tgt.getId());

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

        val profile = new CasProfile();
        profile.setId(ID);
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val ticket = new MockTicketGrantingTicket("casuser");
        configurationContext.getTicketRegistry().addTicket(ticket);
        seedAuthorizeRequest(mockRequest, mockResponse, profile, ticket.getId());

        val result = performAuthorizeRequest(mockRequest);
        assertRedirectsToCasLoginForAuthorize(result);
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
        assertRedirectsToCasLoginForAuthorize(result);
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
        assertRedirectsToCasLoginForAuthorize(result);

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
        assertRedirectsToCasLoginForAuthorize(result);
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
        assertRedirectsToCasLoginForAuthorize(result);
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
        assertRedirectsToCasLoginForAuthorize(result);
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
        assertRedirectsToCasLoginForAuthorize(result);
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
        assertRedirectsToCasLoginForAuthorize(result);
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
        assertRedirectsToCasLoginForAuthorize(result);
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
        configurationContext.getSessionStore().set(new JEEContext(mockRequest, mockResponse), OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");
        mockRequest.setCookies(mockResponse.getCookies());

        val result = performAuthorizeRequest(mockRequest);
        assertRedirectsToCasLoginForAuthorize(result);
    }

    protected CasProfile buildCasProfile() throws Throwable {
        val profile = new CasProfile();
        profile.setId(ID);
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT);
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

    private MvcResult performAuthorizeRequest(final MockHttpServletRequest request) throws Throwable {
        val result = performOAuthRequest(request);
        if (result.getResponse().getCookies() != null && result.getResponse().getCookies().length > 0) {
            request.setCookies(result.getResponse().getCookies());
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
        val queryValue = getParameterValue(uri.getQuery(), parameterName);
        if (StringUtils.isNotBlank(queryValue)) {
            return queryValue;
        }
        return getParameterValue(uri.getFragment(), parameterName);
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
                                      final CasProfile profile) {
        val tgtId = profile.getAttributes().get(TicketGrantingTicket.class.getName());
        seedAuthorizeRequest(request, response, profile, tgtId != null ? tgtId.toString() : null);
    }

    private void seedAuthorizeRequest(final MockHttpServletRequest request,
                                      final MockHttpServletResponse response,
                                      final CasProfile profile,
                                      final @Nullable String ticketGrantingTicketId) {
        val context = new JEEContext(request, response);
        val profiles = CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile);
        val profileManager = new OAuth20ClientIdAwareProfileManager(
            context,
            oauthDistributedSessionStore,
            servicesManager,
            configurationContext.getRequestParameterResolver());
        profileManager.save(true, profile, false);
        oauthDistributedSessionStore.set(context, Pac4jConstants.USER_PROFILES, profiles);
        configurationContext.getSessionStore().set(context, Pac4jConstants.USER_PROFILES, profiles);
        if (StringUtils.isNotBlank(ticketGrantingTicketId)) {
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
