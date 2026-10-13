package org.apereo.cas.support.oauth.profile;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientIdAwareProfileManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20ClientIdAwareProfileManagerTests}.
 *
 * @author Charley Wu
 * @since 6.3.0
 */
@Tag("OAuth")
class OAuth20ClientIdAwareProfileManagerTests extends AbstractOAuth20Tests {
    protected OAuth20ClientIdAwareProfileManager profileManager;

    protected JEEContext context;

    protected OAuthRegisteredService registeredService;

    @BeforeEach
    void init() {
        this.registeredService = addRegisteredService();
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");

        val response = new MockHttpServletResponse();
        context = new JEEContext(request, response);
        profileManager = new OAuth20ClientIdAwareProfileManager(context, oauthDistributedSessionStore,
            servicesManager, oauthRequestParameterResolver);
    }

    @Test
    void verifyGetProfiles() {
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(registeredService.getClientId());
        profileManager.save(true, profile, false);
        val profiles = profileManager.getProfiles();
        assertNotNull(profiles);
        assertEquals(1, profiles.size());
    }

    @Test
    void verifyGetProfilesWithoutSavedClientId() {
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(registeredService.getClientId());
        val sessionProfiles = new HashMap<String, CommonProfile>(1);
        sessionProfiles.put(registeredService.getClientId(), profile);
        oauthDistributedSessionStore.set(context, Pac4jConstants.USER_PROFILES, sessionProfiles);
        val profiles = profileManager.getProfiles();
        assertNotNull(profiles);
        assertEquals(0, profiles.size());
    }

    @Test
    void verifyClientIdFromBasicAuthentication() {
        val manager = mock(ServicesManager.class);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val credentials = registeredService.getClientId() + ':' + CLIENT_SECRET;
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(credentials));
        val basicContext = new JEEContext(request, new MockHttpServletResponse());
        val basicProfileManager = new OAuth20ClientIdAwareProfileManager(basicContext, oauthDistributedSessionStore,
            manager, oauthRequestParameterResolver);
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(registeredService.getClientId());
        basicProfileManager.save(false, profile, false);
        assertEquals(registeredService.getClientId(), profile.getAttribute("oauthClientId"));
        assertEquals(1, basicProfileManager.getProfiles().size());
        verify(manager, never()).getAllServicesOfType(any());
    }

    @Test
    void verifyRedirectUriLookupOncePerRequest() {
        val manager = mock(ServicesManager.class);
        when(manager.getAllServicesOfType(OAuthRegisteredService.class)).thenReturn(List.of(registeredService));
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val redirectContext = new JEEContext(request, new MockHttpServletResponse());
        val redirectProfileManager = new OAuth20ClientIdAwareProfileManager(redirectContext, oauthDistributedSessionStore,
            manager, oauthRequestParameterResolver);
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(registeredService.getClientId());
        redirectProfileManager.save(false, profile, false);
        assertEquals(registeredService.getClientId(), profile.getAttribute("oauthClientId"));
        assertEquals(1, redirectProfileManager.getProfiles().size());
        assertEquals(1, redirectProfileManager.getProfiles().size());
        verify(manager, times(1)).getAllServicesOfType(OAuthRegisteredService.class);
    }

    @Test
    void verifyClientIdFromClientAssertion() {
        val manager = mock(ServicesManager.class);
        val assertion = new PlainJWT(new JWTClaimsSet.Builder()
            .issuer(registeredService.getClientId())
            .subject(registeredService.getClientId())
            .build()).serialize();
        val assertionProfileManager = newProfileManagerWithAssertion(manager, assertion);
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(registeredService.getClientId());
        assertionProfileManager.save(false, profile, false);
        assertEquals(registeredService.getClientId(), profile.getAttribute("oauthClientId"));
        verify(manager, never()).getAllServicesOfType(any());
    }

    @Test
    void verifyClientAssertionWithMismatchedIssuerIsIgnored() {
        val manager = mock(ServicesManager.class);
        when(manager.getAllServicesOfType(OAuthRegisteredService.class)).thenReturn(List.of(registeredService));
        val assertion = new PlainJWT(new JWTClaimsSet.Builder()
            .issuer("another-client")
            .subject(registeredService.getClientId())
            .build()).serialize();
        val assertionProfileManager = newProfileManagerWithAssertion(manager, assertion);
        val profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName(registeredService.getClientId());
        assertionProfileManager.save(false, profile, false);
        assertEquals(registeredService.getClientId(), profile.getAttribute("oauthClientId"));
        verify(manager).getAllServicesOfType(OAuthRegisteredService.class);
    }

    private OAuth20ClientIdAwareProfileManager newProfileManagerWithAssertion(final ServicesManager manager, final String assertion) {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.CLIENT_ASSERTION_TYPE, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
        request.addParameter(OAuth20Constants.CLIENT_ASSERTION, assertion);
        val assertionContext = new JEEContext(request, new MockHttpServletResponse());
        return new OAuth20ClientIdAwareProfileManager(assertionContext, oauthDistributedSessionStore,
            manager, oauthRequestParameterResolver);
    }
}
