package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20ClientIdAwareProfileManager;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This is {@link OAuth20ClientIdAwareProfileManagerTests}.
 *
 * @author Charley Wu
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20ClientIdAwareProfileManagerTests extends AbstractOAuth20Tests {

    protected OAuth20ClientIdAwareProfileManager<CommonProfile> profileManager;
    protected WebContext context;

    @BeforeEach
    public void init() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context = new JEEContext(request, response);
        profileManager = new OAuth20ClientIdAwareProfileManager<>(context, context.getSessionStore(), servicesManager);
    }

    @Test
    public void verifyGetProfiles() {
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(CLIENT_ID);
        profileManager.save(true, profile, false);
        val profiles = profileManager.getAll(true);
        assertNotNull(profiles);
        assertEquals(1, profiles.size());
    }

    @Test
    public void verifyGetProfilesWithoutSavedClientId() {
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(CLIENT_ID);
        val sessionProfiles = new HashMap<String, CommonProfile>(1);
        sessionProfiles.put(CLIENT_ID, profile);
        context.getSessionStore().set(context, Pac4jConstants.USER_PROFILES, sessionProfiles);
        val profiles = profileManager.getAll(true);
        assertNotNull(profiles);
        assertEquals(0, profiles.size());
    }
}
