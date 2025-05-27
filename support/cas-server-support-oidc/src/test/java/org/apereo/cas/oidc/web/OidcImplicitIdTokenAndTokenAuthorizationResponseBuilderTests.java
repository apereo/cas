package org.apereo.cas.oidc.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcImplicitIdTokenAndTokenAuthorizationResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
class OidcImplicitIdTokenAndTokenAuthorizationResponseBuilderTests extends AbstractOidcTests {

    @Test
    void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.IDTOKEN_TOKEN.getType());
        val authzRequest = OAuth20AuthorizationRequest.builder()
            .responseType(OAuth20ResponseTypes.IDTOKEN_TOKEN.getType())
            .build();
        assertTrue(oidcImplicitIdTokenAndTokenCallbackUrlBuilder.supports(authzRequest));
    }

    @Test
    void verifyBuild() throws Throwable {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, List.of("state"));
        attributes.put(OAuth20Constants.NONCE, List.of("nonce"));

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val code = addCode(principal, registeredService);

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        val holder = AccessTokenRequestContext.builder()
            .token(code)
            .clientId(registeredService.getClientId())
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication(principal, attributes))
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.IDTOKEN_TOKEN)
            .userProfile(profile)
            .redirectUri("https://oauth.example.org")
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();

        servicesManager.save(registeredService);
        val mv = oidcImplicitIdTokenAndTokenCallbackUrlBuilder.build(holder);
        assertNotNull(mv);
    }
}
