package org.apereo.cas.oidc.util;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.JEESessionStore;
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
public class OidcRequestSupportTests {

    protected static JEEContext getContextForEndpoint(final String endpoint) {
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("sso.example.org");
        request.setServerPort(8443);
        request.setRequestURI("/cas/oidc/" + endpoint);
        val response = new MockHttpServletResponse();
        return new JEEContext(request, response);
    }

    @Test
    public void verifyRemovePrompt() {
        val url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + '=' + OidcConstants.PROMPT_CONSENT;
        val request = OidcRequestSupport.removeOidcPromptFromAuthorizationRequest(url, OidcConstants.PROMPT_CONSENT);
        assertFalse(request.contains(OidcConstants.PROMPT));
    }

    @Test
    public void verifyOidcPrompt() {
        val url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + "=value1";
        val authorizationRequest = OidcRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        assertEquals("value1", authorizationRequest.toArray()[0]);
    }

    @Test
    public void verifyOidcPromptFromContext() {
        val url = "https://tralala.whapi.com/something?" + OidcConstants.PROMPT + "=value1";
        val context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn(url);
        val authorizationRequest = OidcRequestSupport.getOidcPromptFromAuthorizationRequest(context);
        assertEquals("value1", authorizationRequest.toArray()[0]);
    }

    @Test
    public void verifyOidcMaxAgeTooOld() {
        val context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1");
        val authenticationDate = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5);
        assertTrue(OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authenticationDate));

        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser", authenticationDate);
        assertTrue(OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authn));

        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId("casuser");
        profile.addAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE, authenticationDate);
        assertTrue(OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, profile));
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
        val support = new OidcRequestSupport(builder, registrySupport, mock(OidcIssuerService.class));
        assertTrue(support.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context));
    }

    @Test
    public void verifyOidcMaxAge() {
        val context = mock(WebContext.class);
        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1000");
        val age = OidcRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertTrue(age.isPresent());
        assertEquals((long) age.get(), 1000);

        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=NA");
        val age2 = OidcRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertTrue(age2.isPresent());
        assertEquals((long) age2.get(), -1);

        when(context.getFullRequestURL()).thenReturn("https://tralala.whapi.com/something?");
        val age3 = OidcRequestSupport.getOidcMaxAgeFromAuthorizationRequest(context);
        assertFalse(age3.isPresent());
    }

    @Test
    public void verifyAuthnProfile() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://www.example.org");
        request.setQueryString("param=value");
        val context = new JEEContext(request, new MockHttpServletResponse());
        val profile = new CommonProfile();
        context.setRequestAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertTrue(OidcRequestSupport.isAuthenticationProfileAvailable(context, JEESessionStore.INSTANCE).isPresent());
    }

    @Test
    public void verifyGetRedirectUrlWithError() {
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("example.org");
        request.addParameter("state", "123456");
        request.setServerPort(443);
        val context = new JEEContext(request, new MockHttpServletResponse());
        val expectedUrlWithError = context.getRequestURL() + "?error=login_required&state=123456";
        assertEquals(expectedUrlWithError,
            OidcRequestSupport.getRedirectUrlWithError(context.getRequestURL(), OidcConstants.LOGIN_REQUIRED, context));
    }

    @Test
    public void validateStaticIssuer() {
        val issuerService = mock(OidcIssuerService.class);
        val staticIssuer = "https://sso.example.org:8443/cas/oidc";
        when(issuerService.determineIssuer(any())).thenReturn(staticIssuer);
        val support = new OidcRequestSupport(mock(CasCookieBuilder.class),
            mock(TicketRegistrySupport.class), issuerService);
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("authorize"), "authorize"));
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("profile"), "profile"));
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("logout"), "logout"));
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("realms/authorize"), "authorize"));
    }

    @Test
    public void validateDynamicIssuer() {
        val issuerService = mock(OidcIssuerService.class);
        val staticIssuer = "https://sso.example.org:8443/cas/oidc/custom/fawnoos/issuer";
        when(issuerService.determineIssuer(any())).thenReturn(staticIssuer);
        val support = new OidcRequestSupport(mock(CasCookieBuilder.class),
            mock(TicketRegistrySupport.class), issuerService);
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("custom/fawnoos/issuer/authorize"), "authorize"));
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("custom/fawnoos/issuer/profile"), "profile"));
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("custom/fawnoos/issuer/oidcAuthorize"), "oidcAuthorize"));
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("custom/fawnoos/issuer"), "unknown"));
    }

    @Test
    public void validateDynamicIssuerForLogout() {
        val issuerService = mock(OidcIssuerService.class);
        val staticIssuer = "https://sso.example.org:8443/cas/oidc";
        when(issuerService.determineIssuer(any())).thenReturn(staticIssuer);
        val support = new OidcRequestSupport(mock(CasCookieBuilder.class),
            mock(TicketRegistrySupport.class), issuerService);
        assertTrue(support.isValidIssuerForEndpoint(getContextForEndpoint("logout"), "oidcLogout"));
    }

    @Test
    public void validateIssuerMismatch() {
        val issuerService = mock(OidcIssuerService.class);
        val staticIssuer = "https://sso.example.org:8443/cas/openid-connect";
        when(issuerService.determineIssuer(any())).thenReturn(staticIssuer);
        val support = new OidcRequestSupport(mock(CasCookieBuilder.class),
            mock(TicketRegistrySupport.class), issuerService);
        assertFalse(support.isValidIssuerForEndpoint(getContextForEndpoint("logout"), "oidcLogout"));
    }
}
