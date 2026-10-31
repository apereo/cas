package org.apereo.cas.oidc.util;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.session.DefaultCasSessionStore;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author David Rodriguez
 * @since 5.1.0
 */
@Tag("OIDC")
class OidcRequestSupportTests extends AbstractOidcTests {

    protected static MockHttpServletRequest getRequestForEndpoint(final String endpoint) {
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("sso.example.org");
        request.setServerPort(8443);
        request.setRequestURI("/cas/oidc/" + endpoint);
        return request;
    }

    private static MockHttpServletRequest getRequestForUrl(final String url) {
        val uri = URI.create(url);
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), uri.getRawPath());
        request.setScheme(uri.getScheme());
        request.setServerName(uri.getHost());
        request.setServerPort(uri.getPort() == -1 ? 443 : uri.getPort());
        request.setQueryString(uri.getRawQuery());
        return request;
    }

    @Test
    void verifyRemovePrompt() {
        val url = "https://tralala.whapi.com/something?" + OAuth20Constants.PROMPT + '=' + OidcConstants.PROMPT_CONSENT;
        val request = OidcRequestSupport.removeOidcPromptFromAuthorizationRequest(url, OidcConstants.PROMPT_CONSENT);
        assertFalse(request.contains(OAuth20Constants.PROMPT));
        assertEquals("https://tralala.whapi.com/something?prompt=consent", OidcRequestSupport.removeOidcPromptFromAuthorizationRequest(
            "https://tralala.whapi.com/something?prompt=login+consent", OidcConstants.PROMPT_LOGIN));
        assertTrue(OidcRequestSupport.isZeroMaxAgeAuthorizationRequest("https://tralala.whapi.com/something?max_age=0"));
        assertFalse(OidcRequestSupport.isZeroMaxAgeAuthorizationRequest("https://tralala.whapi.com/something?max_age=10"));
        assertEquals("https://tralala.whapi.com/something?a=b",
            OidcRequestSupport.removeZeroMaxAgeFromAuthorizationRequest("https://tralala.whapi.com/something?max_age=0&a=b"));
        assertEquals("https://tralala.whapi.com/something?scope=openid%20profile&state=a%20b%2Bc",
            OidcRequestSupport.removeOidcPromptFromAuthorizationRequest(
                "https://tralala.whapi.com/something?scope=openid+profile&state=a+b%2Bc&prompt=login", OidcConstants.PROMPT_LOGIN));
        assertEquals("https://tralala.whapi.com/something?scope=openid%20profile&state=a%20b%2Bc",
            OidcRequestSupport.removeZeroMaxAgeFromAuthorizationRequest(
                "https://tralala.whapi.com/something?scope=openid+profile&max_age=0&state=a+b%2Bc"));
    }

    @Test
    void verifyOidcPrompt() {
        val url = "https://tralala.whapi.com/something?" + OAuth20Constants.PROMPT + "=login";
        val authorizationRequest = oauthRequestParameterResolver.resolveSupportedPromptValues(url);
        assertEquals("login", authorizationRequest.toArray()[0]);
        assertEquals(Set.of("login", "consent"), oauthRequestParameterResolver.resolveSupportedPromptValues(url + "+consent"));
        assertEquals(Set.of("login", "consent"), oauthRequestParameterResolver.resolveSupportedPromptValues(url + "%20consent"));
    }

    @Test
    void verifyOidcPromptFromContext() {
        val url = "https://tralala.whapi.com/something?" + OAuth20Constants.PROMPT + "=login";
        val authorizationRequest = oauthRequestParameterResolver.resolveSupportedPromptValues(getRequestForUrl(url));
        assertEquals("login", authorizationRequest.toArray()[0]);

        val postRequest = getRequestForUrl("https://tralala.whapi.com/something");
        postRequest.setMethod(HttpMethod.POST.name());
        postRequest.addParameter(OAuth20Constants.PROMPT, "login consent");
        assertEquals(Set.of("login", "consent"), oauthRequestParameterResolver.resolveSupportedPromptValues(postRequest));
        assertEquals(Set.of("login", "consent"), oauthRequestParameterResolver.resolveRequestedPromptValues(postRequest));
    }

    @Test
    void verifyOidcMaxAgeTooOld() {
        val request = getRequestForUrl("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1");
        val authenticationDate = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5);
        assertTrue(OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(request, authenticationDate));

        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser", authenticationDate);
        assertTrue(OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(request, authn));

        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE, authenticationDate);
        assertTrue(OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(request, profile));
    }

    @Test
    void verifyOidcMaxAgeTooOldForContext() {
        val authenticationDate = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5);
        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser", authenticationDate);

        val request = new MockHttpServletRequest();
        request.setRequestURI("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1");

        val builder = mock(CasCookieBuilder.class);
        when(builder.retrieveCookieValue(any())).thenReturn(UUID.randomUUID().toString());
        val registrySupport = mock(TicketRegistrySupport.class);
        when(registrySupport.getAuthenticationFrom(anyString())).thenReturn(authn);
        val support = new OidcRequestSupport(builder, registrySupport);
        assertTrue(support.isCasAuthenticationOldForMaxAgeAuthorizationRequest(request));
    }

    @Test
    void verifyOidcMaxAge() {
        val age = OidcRequestSupport.getOidcMaxAgeFromAuthorizationRequest(
            getRequestForUrl("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=1000"));
        assertTrue(age.isPresent());
        assertEquals(1000, (long) age.get());

        val age2 = OidcRequestSupport.getOidcMaxAgeFromAuthorizationRequest(
            getRequestForUrl("https://tralala.whapi.com/something?" + OidcConstants.MAX_AGE + "=NA"));
        assertTrue(age2.isPresent());
        assertEquals(-1, (long) age2.get());

        val age3 = OidcRequestSupport.getOidcMaxAgeFromAuthorizationRequest(getRequestForUrl("https://tralala.whapi.com/something?"));
        assertFalse(age3.isPresent());
    }

    @Test
    void verifyAuthnProfile() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://www.example.org");
        request.setQueryString("param=value");
        val response = new MockHttpServletResponse();
        val sessionStore = new DefaultCasSessionStore();
        assertTrue(OidcRequestSupport.isAuthenticationProfileAvailable(request, response, sessionStore).isEmpty());
        val sessionProfile = new DefaultOAuth20UserProfile("casuser");
        sessionStore.set(request, response, OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE, sessionProfile);
        assertSame(sessionProfile, OidcRequestSupport.isAuthenticationProfileAvailable(request, response, sessionStore).orElseThrow());
        val requestProfile = new DefaultOAuth20UserProfile("casuser");
        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, requestProfile);
        assertSame(requestProfile, OidcRequestSupport.isAuthenticationProfileAvailable(request, response, sessionStore).orElseThrow());
    }
}
