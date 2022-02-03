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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcImplicitIdTokenAuthorizationResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcImplicitIdTokenAuthorizationResponseBuilderTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val authzRequest = OAuth20AuthorizationRequest.builder()
            .responseType(OAuth20ResponseTypes.ID_TOKEN.getType())
            .build();
        assertTrue(oidcImplicitIdTokenCallbackUrlBuilder.supports(authzRequest));
    }

    @Test
    public void verifyBuild() throws Exception {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, Collections.singletonList("state"));
        attributes.put(OAuth20Constants.NONCE, Collections.singletonList("nonce"));

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal, attributes);
        val code = addCode(principal, registeredService);

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.ID_TOKEN)
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .token(code)
            .userProfile(profile)
            .redirectUri("https://oauth.example.org")
            .build();

        servicesManager.save(registeredService);
        val mv = oidcImplicitIdTokenCallbackUrlBuilder.build(holder);
        assertNotNull(mv);
    }
}
