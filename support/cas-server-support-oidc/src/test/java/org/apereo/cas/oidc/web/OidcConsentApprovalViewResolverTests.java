package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcConsentApprovalViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
class OidcConsentApprovalViewResolverTests extends AbstractOidcTests {

    @Test
    void verifyBypassedBySession() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        val response = new MockHttpServletResponse();

        val profile = new DefaultOAuth20UserProfile("casTest");
        storeProfileIntoSession(request, profile);

        val service = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        servicesManager.save(service);
        var modelAndView = consentApprovalViewResolver.resolve(request, response, service);
        assertTrue(modelAndView.hasView());
        val model = modelAndView.getModel();
        request.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        request.addParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, model.get("approvalKey").toString());
        modelAndView = consentApprovalViewResolver.resolve(request, response, service);
        assertFalse(modelAndView.hasView());

    }

    @Test
    void verifyBypassedByPrompt() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something");
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.setQueryString(OAuth20Constants.PROMPT + '=' + OidcConstants.PROMPT_CONSENT);

        val response = new MockHttpServletResponse();

        val profile = new DefaultOAuth20UserProfile("casTest");
        storeProfileIntoSession(request, profile);

        val service = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);
        val mv = consentApprovalViewResolver.resolve(request, response, service);
        assertTrue(mv.hasView());

        request.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        request.addParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, "unknown-approval-key");
        assertTrue(consentApprovalViewResolver.resolve(request, response, service).hasView());

        request.setParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, mv.getModel().get("approvalKey").toString());
        assertFalse(consentApprovalViewResolver.resolve(request, response, service).hasView());
    }

    @Test
    void verifyClientIdMetadataDocumentClientsAlwaysConsent() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.addParameter(OAuth20Constants.REDIRECT_URI, "http://127.0.0.1:3000/callback");
        val response = new MockHttpServletResponse();

        val profile = new DefaultOAuth20UserProfile("casTest");
        storeProfileIntoSession(request, profile);

        val service = getOidcRegisteredService("https://app.example.org/" + UUID.randomUUID() + "/client.json", randomServiceUrl());
        service.setBypassApprovalPrompt(true);
        service.getProperties().put(RegisteredServiceProperties.OIDC_CLIENT_ID_METADATA_DOCUMENT.getPropertyName(),
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        servicesManager.save(service);
        val mv = consentApprovalViewResolver.resolve(request, response, service);
        assertTrue(mv.hasView());
        val model = mv.getModel();
        assertEquals(Boolean.TRUE, model.get("clientIdMetadataDocument"));
        assertEquals("app.example.org", model.get("clientIdHost"));
        assertEquals("127.0.0.1", model.get("redirectHost"));
        assertEquals(Boolean.TRUE, model.get("loopbackRedirect"));

        request.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        request.addParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, model.get("approvalKey").toString());
        assertFalse(consentApprovalViewResolver.resolve(request, response, service).hasView());
    }

    @Test
    void verifyBypassedForPushAuthz() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.setRequestURI("https://cas.org/something/" + OidcConstants.PUSHED_AUTHORIZE_URL);
        val response = new MockHttpServletResponse();

        val service = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        servicesManager.save(service);
        val mv = consentApprovalViewResolver.resolve(request, response, service);
        assertFalse(mv.hasView());
    }

    @Test
    void verifyPushedAuthz() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        servicesManager.save(registeredService);

        val profile = new DefaultOAuth20UserProfile("casTest");
        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .userProfile(profile)
            .scopes(CollectionUtils.wrapSet(OidcConstants.StandardScopes.EMAIL.getScope(), OidcConstants.StandardScopes.PROFILE.getScope()))
            .build();
        val factory = (OidcPushedAuthorizationRequestFactory) defaultTicketFactory.get(OidcPushedAuthorizationRequest.class);
        val ticket = factory.create(holder);
        ticketRegistry.addTicket(ticket);

        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something/" + OidcConstants.AUTHORIZE_URL);
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.addParameter(OidcConstants.REQUEST_URI, ticket.getId());


        val response = new MockHttpServletResponse();
        storeProfileIntoSession(request, profile);

        val service = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        servicesManager.save(service);
        val mv = consentApprovalViewResolver.resolve(request, response, service);
        assertTrue(mv.hasView());
        assertEquals(3, ((Collection) mv.getModel().get("scopes")).size());
    }

    @Test
    void verifyBypassedWithoutPrompt() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.setRequestURI("https://cas.org/something");

        val response = new MockHttpServletResponse();

        val profile = new DefaultOAuth20UserProfile("casTest");
        storeProfileIntoSession(request, profile);

        val service = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        service.markAsDynamicallyRegistered();
        servicesManager.save(service);
        val mv = consentApprovalViewResolver.resolve(request, response, service);
        assertTrue(mv.hasView());
        assertTrue(mv.getModel().containsKey("dynamic"));
        assertTrue(mv.getModel().containsKey("dynamicTime"));
    }
}
