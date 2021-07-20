package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAuthenticationAuthorizeSecurityLogicTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
public class OidcAuthenticationAuthorizeSecurityLogicTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, JEESessionStore.INSTANCE);
        profileManager.save(true, new BasicUserProfile(), false);
        val logic = new OidcAuthenticationAuthorizeSecurityLogic();
        assertFalse(logic.loadProfiles(profileManager, context, JEESessionStore.INSTANCE, List.of()).isEmpty());
        request.setQueryString("prompt=login");
        assertTrue(logic.loadProfiles(profileManager, context, JEESessionStore.INSTANCE, List.of()).isEmpty());
    }
}
