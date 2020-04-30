package org.apereo.cas.oidc.util;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
@Tag("OIDC")
public class OidcAuthorizationRequestSupportTests {

    @Test
    public void verifyRemovePrompt() {
        val url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + '=' + OidcConstants.PROMPT_CONSENT;
        val request = OidcAuthorizationRequestSupport.removeOidcPromptFromAuthorizationRequest(url, OidcConstants.PROMPT_CONSENT);
        assertFalse(request.contains(OidcConstants.PROMPT));
    }

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
    public void verifyOidcMaxAgeTooOld() {
        val context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1");
        val authenticationDate = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5);
        assertTrue(OidcAuthorizationRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authenticationDate));

        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser", authenticationDate);
        assertTrue(OidcAuthorizationRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authn));

        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId("casuser");
        profile.addAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE, authenticationDate);
        assertTrue(OidcAuthorizationRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, profile));
    }

    @Test
    public void verifyOidcMaxAgeTooOldForContext() {
        val authenticationDate = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5);
        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser", authenticationDate);

        val request = new MockHttpServletRequest();
        request.setRequestURI("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val builder = mock(CasCookieBuilder.class);
        when(builder.retrieveCookieValue(any())).thenReturn(UUID.randomUUID().toString());
        val registrySupport = mock(TicketRegistrySupport.class);
        when(registrySupport.getAuthenticationFrom(anyString())).thenReturn(authn);
        val support = new OidcAuthorizationRequestSupport(builder, registrySupport);
        assertTrue(support.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context));
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
        val profile = new CommonProfile();
        context.setRequestAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertTrue(OidcAuthorizationRequestSupport.isAuthenticationProfileAvailable(context).isPresent());
    }

    @Test
    public void verifyGetRedirectUrlWithError() {
        val originalRedirectUrl = "https://www.example.org";
        val expectedUrlWithError = originalRedirectUrl + "?error=login_required";
        assertEquals(expectedUrlWithError, OidcAuthorizationRequestSupport.getRedirectUrlWithError(originalRedirectUrl, OidcConstants.LOGIN_REQUIRED));
    }
}
