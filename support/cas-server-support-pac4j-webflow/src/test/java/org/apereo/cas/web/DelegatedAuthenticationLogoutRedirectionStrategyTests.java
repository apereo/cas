package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.logout.DelegatedAuthenticationLogoutRedirectionStrtategy;
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
 * This is {@link DelegatedAuthenticationLogoutRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DelegatedAuthenticationLogoutRedirectionStrategyTests {
    @Autowired
    @Qualifier("delegatedAuthenticationLogoutRedirectionStrtategy")
    private DelegatedAuthenticationLogoutRedirectionStrtategy delegatedAuthenticationLogoutRedirectionStrtategy;

    @Test
    void verifySupportsReturnsFalseWhenNoLogoutRequestPresent() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertFalse(delegatedAuthenticationLogoutRedirectionStrtategy.supports(request, response));
    }

    @Test
    void verifySupportsReturnsTrueWhenLogoutRequestPresent() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder()
            .status(HttpServletResponse.SC_FOUND).build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        assertTrue(delegatedAuthenticationLogoutRedirectionStrtategy.supports(request, response));
    }

    @Test
    void verifyHandleWithRedirectLocationAndFoundStatus() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val location = "https://idp.example.org/logout";
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder()
            .status(HttpServletResponse.SC_FOUND)
            .location(location)
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedAuthenticationLogoutRedirectionStrtategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getLogoutRedirectUrl().isPresent());
        assertEquals(location, result.getLogoutRedirectUrl().get());
    }

    @Test
    void verifyHandleWithBlankLocationDoesNotRedirect() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder()
            .status(HttpServletResponse.SC_FOUND)
            .location("   ")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedAuthenticationLogoutRedirectionStrtategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyHandleWithLocationButNonFoundStatusDoesNotRedirect() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder()
            .status(HttpServletResponse.SC_OK)
            .location("https://idp.example.org/logout")
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        
        val result = delegatedAuthenticationLogoutRedirectionStrtategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyHandleExtractsServiceFromRequest() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter("service", "https://app.example.org");
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder()
            .status(HttpServletResponse.SC_OK)
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedAuthenticationLogoutRedirectionStrtategy.handle(request, response);
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
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(request, logoutRequest);
        val result = delegatedAuthenticationLogoutRedirectionStrtategy.handle(request, response);
        assertNotNull(result);
        assertTrue(result.getService().isEmpty());
        assertTrue(result.getLogoutRedirectUrl().isEmpty());
    }
}
