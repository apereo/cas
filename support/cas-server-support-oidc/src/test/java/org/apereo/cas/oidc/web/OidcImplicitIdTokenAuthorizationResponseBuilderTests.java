package org.apereo.cas.oidc.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.ID_TOKEN.getType());
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertTrue(oidcImplicitIdTokenCallbackUrlBuilder.supports(context));
    }

    @Test
    public void verifyBuild() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, Collections.singletonList("state"));
        attributes.put(OAuth20Constants.NONCE, Collections.singletonList("nonce"));

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal, attributes);
        val code = addCode(principal, registeredService);
        val holder = AccessTokenRequestDataHolder.builder()
            .clientId(registeredService.getClientId())
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .token(code)
            .build();

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.ID_TOKEN.getType());
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, JEESessionStore.INSTANCE);

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        manager.save(true, profile, false);
        servicesManager.save(registeredService);
        val mv = oidcImplicitIdTokenCallbackUrlBuilder.build(context, registeredService.getClientId(), holder);
        assertNotNull(mv);
    }
}
