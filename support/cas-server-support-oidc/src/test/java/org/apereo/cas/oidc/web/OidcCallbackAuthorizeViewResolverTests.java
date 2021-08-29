package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCallbackAuthorizeViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcCallbackAuthorizeViewResolverTests extends AbstractOidcTests {

    @Test
    public void verifyRedirect() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, JEESessionStore.INSTANCE);

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);

        val mv = callbackAuthorizeViewResolver.resolve(context, manager, "https://github.com");
        assertNotNull(mv);
        assertTrue(mv.getView() instanceof RedirectView);
    }

    @Test
    public void verifyPromptNoneWithProfile() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, JEESessionStore.INSTANCE);

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);
        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
        assertTrue(mv.getView() instanceof RedirectView);
    }

    @Test
    public void verifyPromptNoneWithoutProfile() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, JEESessionStore.INSTANCE);

        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
        assertEquals(mv.getModel().get(OAuth20Constants.ERROR), OidcConstants.LOGIN_REQUIRED);
    }

    @Test
    public void verifyPromptNoneWithoutProfileWithPostResponseMode() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://google.com");
        request.addParameter(OAuth20Constants.STATE, "randomState");
        request.addParameter(OAuth20Constants.NONCE, "nonce");
        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        request.addParameter(OidcConstants.PROMPT, "none");
        val manager = new ProfileManager(context, JEESessionStore.INSTANCE);

        val builder = new URIBuilder();
        request.getParameterMap().forEach((key, values) -> builder.addParameter(key, values[0]));
        request.setQueryString(builder.build().getQuery());
        val url = context.getFullRequestURL();
        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
        assertEquals(CasWebflowConstants.VIEW_ID_POST_RESPONSE, mv.getViewName());
        assertTrue(mv.getModel().containsKey("originalUrl"));
        var parameters = (Map<String, String>) mv.getModel().get("parameters");
        assertEquals(request.getParameter(OAuth20Constants.STATE), parameters.get(OAuth20Constants.STATE));
        assertFalse(parameters.containsKey(OAuth20Constants.NONCE));
    }

    @Test
    public void verifyPromptNoneWithoutProfileWithRedirectParam() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://google.com");
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, JEESessionStore.INSTANCE);

        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
        assertEquals(mv.getModel().get(OAuth20Constants.ERROR), OidcConstants.LOGIN_REQUIRED);
    }

    @Test
    public void verifyPromptLogin() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=login";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, JEESessionStore.INSTANCE);

        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
        assertTrue(mv.getView() instanceof RedirectView);
    }
}
