package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCallbackAuthorizeViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
class OidcCallbackAuthorizeViewResolverTests extends AbstractOidcTests {

    @Test
    void verifyRedirect() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val profile = new DefaultOAuth20UserProfile("casuser");

        val mv = callbackAuthorizeViewResolver.resolve(request, response, profile, "https://github.com");
        assertNotNull(mv);
        assertInstanceOf(RedirectView.class, mv.getView());
    }

    @Test
    void verifyPromptNoneWithProfile() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val profile = new DefaultOAuth20UserProfile("casuser");
        val sessionOnly = callbackAuthorizeViewResolver.resolve(request, response, profile, url);
        assertEquals(OidcConstants.LOGIN_REQUIRED, sessionOnly.getModel().get(OAuth20Constants.ERROR));

        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, profile);
        val mv = callbackAuthorizeViewResolver.resolve(request, response, profile, url);
        assertNotNull(mv);
        assertInstanceOf(RedirectView.class, mv.getView());
    }

    @Test
    void verifyPromptNoneWithProfileNotFreshEnough() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
            ZonedDateTime.now(ZoneOffset.UTC).minusHours(1).toString());
        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, profile);
        val urls = List.of(
            "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none&" + OidcConstants.MAX_AGE + "=0",
            "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none&" + OidcConstants.MAX_AGE + "=60",
            "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none+login");
        urls.forEach(url -> assertEquals(OidcConstants.LOGIN_REQUIRED,
            callbackAuthorizeViewResolver.resolve(request, response, profile, url).getModel().get(OAuth20Constants.ERROR)));

        val fresh = "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none&" + OidcConstants.MAX_AGE + "=7200";
        assertInstanceOf(RedirectView.class, callbackAuthorizeViewResolver.resolve(request, response, profile, fresh).getView());
    }

    @Test
    void verifyPromptNoneWithoutProfile() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val mv = callbackAuthorizeViewResolver.resolve(request, response, null, url);
        assertNotNull(mv);
        assertEquals(OidcConstants.LOGIN_REQUIRED, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    void verifyPromptNoneWithoutProfileWithPostResponseMode() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), "^https://google\\.com.*");
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://google.com");
        request.addParameter(OAuth20Constants.STATE, "randomState");
        request.addParameter(OAuth20Constants.NONCE, "nonce");
        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        request.addParameter(OAuth20Constants.PROMPT, "none");
        val builder = new URIBuilder();
        request.getParameterMap().forEach((key, values) -> builder.addParameter(key, values[0]));
        request.setQueryString(builder.build().getQuery());
        val url = HttpRequestUtils.getFullRequestUrl(request);
        val mv = callbackAuthorizeViewResolver.resolve(request, response, null, url);
        assertNotNull(mv);
        assertEquals(CasWebflowConstants.VIEW_ID_POST_RESPONSE, mv.getViewName());
        assertTrue(mv.getModel().containsKey("originalUrl"));
        var parameters = (Map<String, String>) mv.getModel().get("parameters");
        assertEquals(request.getParameter(OAuth20Constants.STATE), parameters.get(OAuth20Constants.STATE));
        assertFalse(parameters.containsKey(OAuth20Constants.NONCE));
    }

    @Test
    void verifyPromptNoneWithoutProfileWithRedirectParam() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://google.com");
        val url = "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val mv = callbackAuthorizeViewResolver.resolve(request, response, null, url);
        assertNotNull(mv);
        assertEquals(OidcConstants.LOGIN_REQUIRED, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    void verifyPromptNoneWithoutProfileRedirectsOnce() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), "^https://client\\.example\\.org/cb.*");
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://client.example.org/cb?x=1");
        request.addParameter(OAuth20Constants.STATE, "s1");
        request.addParameter(OAuth20Constants.PROMPT, OidcConstants.PROMPT_NONE);
        val url = "https://cas.org/something?" + OAuth20Constants.PROMPT + "=none";
        val mv = callbackAuthorizeViewResolver.resolve(request, new MockHttpServletResponse(), null, url);
        val view = assertInstanceOf(RedirectView.class, mv.getView());
        assertEquals("https://client.example.org/cb?x=1", view.getUrl());
        assertEquals(OidcConstants.LOGIN_REQUIRED, mv.getModel().get(OAuth20Constants.ERROR));
        assertEquals("s1", mv.getModel().get(OAuth20Constants.STATE));

        val unregistered = new MockHttpServletRequest();
        unregistered.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        unregistered.addParameter(OAuth20Constants.REDIRECT_URI, "https://attacker.example.org/cb");
        unregistered.addParameter(OAuth20Constants.PROMPT, OidcConstants.PROMPT_NONE);
        val refused = callbackAuthorizeViewResolver.resolve(unregistered, new MockHttpServletResponse(), null, url);
        assertFalse(refused.getView() instanceof RedirectView);
        assertEquals(OidcConstants.LOGIN_REQUIRED, refused.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    void verifyPromptLogin() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OAuth20Constants.PROMPT + "=login%20consent&" + OidcConstants.MAX_AGE + "=0";
        val response = new MockHttpServletResponse();
        val mv = callbackAuthorizeViewResolver.resolve(request, response, null, url);
        assertEquals(url, assertInstanceOf(RedirectView.class, mv.getView()).getUrl());

        val profile = new DefaultOAuth20UserProfile("casuser");
        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, profile);
        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_NEW_LOGIN, Boolean.FALSE);
        val afterSingleSignOn = callbackAuthorizeViewResolver.resolve(request, response, profile, url);
        assertEquals(url, assertInstanceOf(RedirectView.class, afterSingleSignOn.getView()).getUrl());

        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_NEW_LOGIN, Boolean.TRUE);
        val afterLogin = callbackAuthorizeViewResolver.resolve(request, response, profile, url);
        assertEquals("https://cas.org/something?" + OAuth20Constants.PROMPT + "=consent",
            assertInstanceOf(RedirectView.class, afterLogin.getView()).getUrl());
    }
}
