package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AccessTokenSecurityLogicTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OAuth")
public class OAuth20AccessTokenSecurityLogicTests extends AbstractOAuth20Tests {

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        val logic = new OAuth20AccessTokenSecurityLogic();
        val mockClient = mock(DirectClient.class);
        when(mockClient.getName()).thenReturn("MockIndirectClient");
        when(mockClient.isInitialized()).thenReturn(true);
        when(mockClient.getCredentials(any(), any()))
            .thenReturn(Optional.of(new UsernamePasswordCredentials("casuser", "Mellon")));
        val profile = new CommonProfile();
        profile.setId(UUID.randomUUID().toString());
        when(mockClient.getUserProfile(any(), any(), any())).thenReturn(Optional.of(profile));

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, JEESessionStore.INSTANCE);
        profileManager.save(true, profile, false);

        val result = (UserProfile) logic.perform(context, JEESessionStore.INSTANCE,
            new Config(mockClient),
            (webContext, sessionStore, collection, objects) -> collection.iterator().next(),
            JEEHttpActionAdapter.INSTANCE, "MockIndirectClient",
            DefaultAuthorizers.IS_FULLY_AUTHENTICATED, DefaultMatchers.SECURITYHEADERS);
        assertNotNull(result);
        assertEquals(1, profileManager.getProfiles().size());
    }

}
