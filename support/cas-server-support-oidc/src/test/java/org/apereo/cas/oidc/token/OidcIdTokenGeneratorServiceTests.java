package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apereo.cas.oidc.OidcConstants.StandardScopes.EMAIL;
import static org.apereo.cas.oidc.OidcConstants.StandardScopes.OPENID;
import static org.apereo.cas.oidc.OidcConstants.StandardScopes.PROFILE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcIdTokenGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
public class OidcIdTokenGeneratorServiceTests extends AbstractOidcTests {

    private static final String OIDC_CLAIM_EMAIL = "email";
    private static final String OIDC_CLAIM_PHONE_NUMBER = "phone_number";
    private static final String OIDC_CLAIM_NAME = "name";

    @Test
    public void verifyTokenGeneration() throws Exception {
        val request = new MockHttpServletRequest();
        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId("casuser");

        request.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        val response = new MockHttpServletResponse();

        val tgt = mock(TicketGrantingTicket.class);
        val callback = casProperties.getServer().getPrefix()
            + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;

        val service = new WebApplicationServiceFactory().createService(callback);
        when(tgt.getServices()).thenReturn(CollectionUtils.wrap("service", service));

        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"),
                OIDC_CLAIM_PHONE_NUMBER, List.of("123456789"),
                OIDC_CLAIM_NAME, List.of("casuser")));

        var authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                OAuth20Constants.NONCE, List.of("some-nonce")));
        when(tgt.getAuthentication()).thenReturn(authentication);

        val accessToken = mock(AccessToken.class);
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getId()).thenReturn(getClass().getSimpleName());
        when(accessToken.getScopes()).thenReturn(Set.of(OPENID.getScope(), PROFILE.getScope(), EMAIL.getScope()));

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid");
        val idToken = oidcIdTokenGenerator.generate(request, response, accessToken, 30,
            OAuth20ResponseTypes.CODE, registeredService);
        assertNotNull(idToken);

        val claims = oidcTokenSigningAndEncryptionService.decode(idToken, Optional.ofNullable(registeredService));
        assertNotNull(claims);
        assertTrue(claims.hasClaim(OIDC_CLAIM_EMAIL));
        assertTrue(claims.hasClaim(OIDC_CLAIM_NAME));
        assertFalse(claims.hasClaim(OIDC_CLAIM_PHONE_NUMBER));
        assertEquals("casuser@example.org", claims.getStringClaimValue(OIDC_CLAIM_EMAIL));
        assertEquals("casuser", claims.getStringClaimValue(OIDC_CLAIM_NAME));
    }

    @Test
    public void verifyTokenGenerationWithoutCallbackService() {
        val request = new MockHttpServletRequest();
        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId("casuser");
        request.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        val response = new MockHttpServletResponse();

        val tgt = mock(TicketGrantingTicket.class);

        when(tgt.getServices()).thenReturn(new HashMap<>());
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser",
            CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                OAuth20Constants.NONCE, List.of("some-nonce")));
        when(tgt.getAuthentication()).thenReturn(authentication);

        val accessToken = mock(AccessToken.class);
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getId()).thenReturn(getClass().getSimpleName());

        val idToken = oidcIdTokenGenerator.generate(request, response, accessToken, 30,
            OAuth20ResponseTypes.CODE, OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid"));
        assertNotNull(idToken);
    }

    @Test
    public void verifyTokenGenerationFailsWithoutProfile() {
        assertThrows(IllegalArgumentException.class, () -> {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val accessToken = mock(AccessToken.class);
            oidcIdTokenGenerator.generate(request, response, accessToken, 30,
                OAuth20ResponseTypes.CODE,
                OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid"));
        });
    }
}
