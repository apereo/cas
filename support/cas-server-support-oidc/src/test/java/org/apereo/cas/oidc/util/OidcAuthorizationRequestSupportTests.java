package org.apereo.cas.oidc.util;

import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
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
        val context = mock(WebContext.class);
        when(context.getSessionStore()).thenReturn(mock(SessionStore.class));
        when(context.getRequestAttribute(anyString())).thenReturn(new CommonProfile());
        assertTrue(OidcAuthorizationRequestSupport.isAuthenticationProfileAvailable(context).isPresent());
    }
}
