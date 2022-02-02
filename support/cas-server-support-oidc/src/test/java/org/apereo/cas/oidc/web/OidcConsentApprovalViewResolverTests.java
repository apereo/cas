package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcConsentApprovalViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcConsentApprovalViewResolverTests extends AbstractOidcTests {

    @Test
    public void verifyBypassedBySession() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        JEESessionStore.INSTANCE.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");
        val service = getOAuthRegisteredService(UUID.randomUUID().toString(), "https://google.com");
        assertFalse(consentApprovalViewResolver.resolve(context, service).hasView());
    }

    @Test
    public void verifyBypassedByPrompt() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something");
        request.setQueryString(OidcConstants.PROMPT + '=' + OidcConstants.PROMPT_CONSENT);

        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        val mv = consentApprovalViewResolver.resolve(context, service);
        assertTrue(mv.hasView());
    }

    @Test
    public void verifyBypassedForPushAuthz() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something/" + OidcConstants.PUSHED_AUTHORIZE_URL);
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        val mv = consentApprovalViewResolver.resolve(context, service);
        assertFalse(mv.hasView());
    }

    @Test
    public void verifyPushedAuthz() throws Exception {
        val registeredService = getOidcRegisteredService();
        val profile = new CommonProfile();
        profile.setId("casTest");
        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .userProfile(profile)
            .scopes(CollectionUtils.wrapSet("email", "profile"))
            .build();
        val factory = (OidcPushedAuthorizationRequestFactory) defaultTicketFactory.get(OidcPushedAuthorizationRequest.class);
        val ticket = factory.create(holder);
        ticketRegistry.addTicket(ticket);

        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something/" + OidcConstants.AUTHORIZE_URL);
        request.addParameter(OidcConstants.REQUEST_URI, ticket.getId());
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        val mv = consentApprovalViewResolver.resolve(context, service);
        assertTrue(mv.hasView());
        assertEquals(3, ((Collection) mv.getModel().get("scopes")).size());
    }

    @Test
    public void verifyBypassedWithoutPrompt() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something");

        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        val mv = consentApprovalViewResolver.resolve(context, service);
        assertTrue(mv.hasView());
    }
}
