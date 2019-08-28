package org.apereo.cas.oidc.util;

import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
@Tag("OIDC")
public class OidcAuthorizationRequestSupportTests {

    @Test
    public void verifyOidcPrompt() {
        val url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + "=value1";
        val authorizationRequest = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        assertEquals("value1", authorizationRequest.toArray()[0]);
    }

    @Test
    public void verifyOidcPromptFromContext() {
        val url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + "=value1";
        val context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn(url);
        val authorizationRequest = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(context);
        assertEquals("value1", authorizationRequest.toArray()[0]);
    }

    @Test
    public void verifyOidcMaxAge() {
        val context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1000");
        val age = OidcAuthorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertTrue(age.isPresent());
        assertTrue(1000 == age.get());

        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=NA");
        val age2 = OidcAuthorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertTrue(age2.isPresent());
        assertTrue(-1 == age2.get());

        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?");
        val age3 = OidcAuthorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertFalse(age3.isPresent());
    }

    @Test
    public void verifyAuthnProfile() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://www.example.org");
        request.setQueryString("param=value");
        val context = new JEEContext(request, new MockHttpServletResponse(), mock(SessionStore.class));
        context.setRequestAttribute(Pac4jConstants.USER_PROFILES, new CommonProfile());
        assertTrue(OidcAuthorizationRequestSupport.isAuthenticationProfileAvailable(context).isPresent());
    }

    @Test
    public void verifyGetRedirectUrlWithError() {
        val originalRedirectUrl = "https://www.example.org";
        val expectedUrlWithError = originalRedirectUrl + "?error=login_required";
        assertEquals(expectedUrlWithError, OidcAuthorizationRequestSupport.getRedirectUrlWithError(originalRedirectUrl, OidcConstants.LOGIN_REQUIRED));
    }
}
