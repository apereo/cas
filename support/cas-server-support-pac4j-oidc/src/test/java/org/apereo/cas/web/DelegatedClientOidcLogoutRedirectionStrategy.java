package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.config.CasDelegatedAuthenticationOidcAutoConfiguration;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientOidcLogoutRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(
    classes = {
        CasDelegatedAuthenticationOidcAutoConfiguration.class,
        BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
    },
    properties = {
        "cas.authn.pac4j.oidc[0].google.client-name=GoogleClient",
        "cas.authn.pac4j.oidc[0].google.id=61201e00-c304-4570-bc18-52c5710367ac",
        "cas.authn.pac4j.oidc[0].google.secret=8dcff64a-1e06-42d5-8f6a-72f98de05874",
        "cas.authn.pac4j.oidc[0].google.discovery-uri=https://localhost:8443/.well-known/openid-configuration"
    })
@Tag("Delegation")
class DelegatedClientOidcLogoutRedirectionStrategy {
    @Autowired
    @Qualifier("delegatedOidcLogoutRedirectionStrategy")
    private LogoutRedirectionStrategy delegatedOidcLogoutRedirectionStrategy;

    @Test
    void verifySupportsReturnsFalseWhenNoLogoutRequestPresent() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertFalse(delegatedOidcLogoutRedirectionStrategy.supports(request, response));
    }

    @Test
    void verifySupportsReturnsTrueWhenLogoutRequestPresent() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest
            .builder()
            .status(HttpServletResponse.SC_FOUND)
            .clientName("GoogleClient")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        assertTrue(delegatedOidcLogoutRedirectionStrategy.supports(request, response));
    }

    @Test
    void verifyHandleWithRedirectLocationAndFoundStatus() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val location = "https://idp.example.org/logout";
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest
            .builder()
            .status(HttpServletResponse.SC_FOUND)
            .location(location)
            .clientName("GoogleClient")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedOidcLogoutRedirectionStrategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getLogoutRedirectUrl().isPresent());
        assertEquals(location, result.getLogoutRedirectUrl().get());
    }

    @Test
    void verifyHandleWithBlankLocationDoesNotRedirect() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest
            .builder()
            .status(HttpServletResponse.SC_FOUND)
            .location("   ")
            .clientName("GoogleClient")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedOidcLogoutRedirectionStrategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyHandleWithLocationButNonFoundStatusDoesNotRedirect() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest
            .builder()
            .status(HttpServletResponse.SC_OK)
            .location("https://idp.example.org/logout")
            .clientName("GoogleClient")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        
        val result = delegatedOidcLogoutRedirectionStrategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyHandleExtractsServiceFromRequest() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter("service", "https://app.example.org");
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest
            .builder()
            .status(HttpServletResponse.SC_OK)
            .clientName("GoogleClient")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedOidcLogoutRedirectionStrategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getService().isPresent());
        assertEquals("https://app.example.org", result.getService().get().getId());
    }

    @Test
    void verifyHandleWithNoServiceParameter() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder()
            .status(HttpServletResponse.SC_OK)
            .clientName("GoogleClient")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedOidcLogoutRedirectionStrategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getService().isEmpty());
        assertTrue(result.getLogoutRedirectUrl().isEmpty());
    }
}
