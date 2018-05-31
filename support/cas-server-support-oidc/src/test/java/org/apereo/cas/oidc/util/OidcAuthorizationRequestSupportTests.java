package org.apereo.cas.oidc.util;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
@Slf4j
public class OidcAuthorizationRequestSupportTests {

    @Test
    public void verifyOidcPrompt() {
        final var url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + "=value1";
        final var authorizationRequest = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        assertEquals("value1", authorizationRequest.toArray()[0]);
    }

    @Test
    public void verifyOidcPromptFromContext() {
        final var url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + "=value1";
        final var context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn(url);
        final var authorizationRequest = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(context);
        assertEquals("value1", authorizationRequest.toArray()[0]);
    }

    @Test
    public void verifyOidcMaxAge() {
        final var context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1000");
        var age = OidcAuthorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertTrue(age.isPresent());
        assertTrue(1000 == age.get());

        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=NA");
        age = OidcAuthorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertTrue(age.isPresent());
        assertTrue(-1 == age.get());

        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?");
        age = OidcAuthorizationRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertFalse(age.isPresent());
    }

    @Test
    public void verifyAuthnProfile() {
        final var context = mock(WebContext.class);
        when(context.getSessionStore()).thenReturn(mock(SessionStore.class));
        when(context.getRequestAttribute(anyString())).thenReturn(new CommonProfile());
        assertTrue(OidcAuthorizationRequestSupport.isAuthenticationProfileAvailable(context).isPresent());
    }
}
