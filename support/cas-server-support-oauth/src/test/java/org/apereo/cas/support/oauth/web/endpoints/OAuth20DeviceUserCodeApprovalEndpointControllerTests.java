package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DeviceUserCodeApprovalEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuthWeb")
class OAuth20DeviceUserCodeApprovalEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Test
    void verifyGet() throws Throwable {
        val result = performOAuthRequest(prepareDeviceApprovalRequest(HttpMethod.GET, null));
        assertEquals(org.springframework.http.HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertNotNull(result.getResponse().getRedirectedUrl());
        assertTrue(result.getResponse().getRedirectedUrl().contains(OAuth20Constants.CALLBACK_AUTHORIZE_URL));
    }

    @Test
    void verifyPostNoCode() throws Throwable {
        var result = performOAuthRequest(prepareDeviceApprovalRequest(HttpMethod.POST, null));
        assertEquals(org.springframework.http.HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertNotNull(result.getResponse().getRedirectedUrl());
        assertFalse(result.getResponse().getRedirectedUrl().contains(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE));
        val id = UUID.randomUUID().toString();
        result = performOAuthRequest(prepareDeviceApprovalRequest(HttpMethod.POST, id));
        assertEquals(org.springframework.http.HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertNotNull(result.getResponse().getRedirectedUrl());
        assertTrue(result.getResponse().getRedirectedUrl().contains(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE));
    }

    @Test
    void verifyApproval() throws Throwable {
        val devCode = defaultDeviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService());
        val uc = defaultDeviceUserCodeFactory.createDeviceUserCode(devCode.getService());
        ticketRegistry.addTicket(uc);
        var result = performOAuthRequest(prepareDeviceApprovalRequest(HttpMethod.POST, uc.getId()));
        assertEquals(org.springframework.http.HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertNotNull(result.getResponse().getRedirectedUrl());
        assertTrue(result.getResponse().getRedirectedUrl().contains(OAuth20Constants.CALLBACK_AUTHORIZE_URL));
        assertTrue(result.getResponse().getRedirectedUrl().contains(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE));
        assertFalse(uc.isUserCodeApproved());

        result = performOAuthRequest(prepareDeviceApprovalRequest(HttpMethod.POST, uc.getId()));
        assertEquals(org.springframework.http.HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertNotNull(result.getResponse().getRedirectedUrl());
        assertTrue(result.getResponse().getRedirectedUrl().contains(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE));
    }

    private MockHttpServletRequest prepareDeviceApprovalRequest(final HttpMethod method, final String userCode) throws Exception {
        val request = new MockHttpServletRequest(method.name(), CONTEXT + OAuth20Constants.DEVICE_AUTHZ_URL);
        val session = new MockHttpSession();
        request.setSession(session);
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        if (userCode != null) {
            request.setParameter(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE, userCode);
        }

        val commonProfile = new CommonProfile();
        commonProfile.setId("testuser");
        commonProfile.setClientName(Authenticators.CAS_OAUTH_CLIENT);
        val response = new MockHttpServletResponse();
        oauthDistributedSessionStore.set(new JEEContext(request, response), Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(commonProfile.getClientName(), commonProfile));

        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        ticketRegistry.addTicket(tgt);
        val cookie = ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        assertNotNull(cookie);
        request.setCookies(response.getCookies());
        return request;
    }
}
