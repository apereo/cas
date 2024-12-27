package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.JEEContextFactory;
import org.pac4j.jee.context.JEEFrameworkParameters;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.jee.context.session.JEESessionStoreFactory;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
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
@Tag("OAuthToken")
class OAuth20AccessTokenSecurityLogicTests extends AbstractOAuth20Tests {

    @Test
    void verifyOperation() {
        val registeredService = addRegisteredService();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val logic = new DefaultSecurityLogic();
        logic.setLoadProfilesFromSession(false);
        
        val mockClient = mock(DirectClient.class);
        when(mockClient.getName()).thenReturn("MockIndirectClient");
        when(mockClient.isInitialized()).thenReturn(true);
        val testCredential = new UsernamePasswordCredentials("casuser", "Mellon");
        when(mockClient.getCredentials(any())).thenReturn(Optional.of(testCredential));
        when(mockClient.validateCredentials(any(), any())).thenReturn(Optional.of(testCredential));
        val profile = new CommonProfile();
        profile.setId(UUID.randomUUID().toString());
        when(mockClient.getUserProfile(any(), any())).thenReturn(Optional.of(profile));

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, new JEESessionStore());
        profileManager.save(true, profile, false);

        val config = new Config(mockClient);
        config.setSessionStoreFactory(JEESessionStoreFactory.INSTANCE);
        config.setHttpActionAdapter(JEEHttpActionAdapter.INSTANCE);
        config.setWebContextFactory(JEEContextFactory.INSTANCE);
        config.setProfileManagerFactory((webContext, sessionStore) -> profileManager);
        
        val result = (UserProfile) logic.perform(config,
            (webContext, sessionStore, collection) -> collection.iterator().next(),
            "MockIndirectClient",
            DefaultAuthorizers.IS_FULLY_AUTHENTICATED, DefaultMatchers.SECURITYHEADERS,
            new JEEFrameworkParameters(request, response));
        assertNotNull(result);
        assertEquals(1, profileManager.getProfiles().size());
    }

}
