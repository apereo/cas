package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);

        val mv = callbackAuthorizeViewResolver.resolve(context, manager, "https://github.com");
        assertNotNull(mv);
    }

    @Test
    public void verifyPromptNoneWithProfile() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);
        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
    }

    @Test
    public void verifyPromptNoneWithoutProfile() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());
        
        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
    }

    @Test
    public void verifyPromptNoneWithoutProfileWithRedirectParam() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://google.com");
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=none";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());

        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
    }

    @Test
    public void verifyPromptLogin() {
        val request = new MockHttpServletRequest();
        val url = "https://cas.org/something?" + OidcConstants.PROMPT + "=login";
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());

        val mv = callbackAuthorizeViewResolver.resolve(context, manager, url);
        assertNotNull(mv);
    }
}
