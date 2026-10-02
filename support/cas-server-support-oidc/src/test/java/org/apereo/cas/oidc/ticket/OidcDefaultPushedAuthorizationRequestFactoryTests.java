package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultPushedAuthorizationRequestFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
class OidcDefaultPushedAuthorizationRequestFactoryTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val profile = new CommonProfile();
        profile.setId("casTest");
        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .userProfile(profile)
            .build();
        val factory = (OidcPushedAuthorizationRequestFactory) defaultTicketFactory.get(OidcPushedAuthorizationRequest.class);
        val ticket = factory.create(holder);
        assertNotNull(ticket);
        assertTrue(ticket.getId().startsWith(OidcPushedAuthorizationRequest.PREFIX));
        assertSame(OidcPushedAuthorizationRequest.class, factory.getTicketType());

        val expiration = Beans.newDuration(casProperties.getAuthn().getOidc().getPar().getMaxTimeToLiveInSeconds()).toSeconds();
        assertEquals(expiration, ticket.getExpirationPolicy().getTimeToLive());

        val atRequest = factory.toAccessTokenRequest(ticket);
        assertEquals(holder.getAuthentication(), atRequest.getAuthentication());
        assertEquals(holder.getService(), atRequest.getService());
        assertEquals(holder.getRegisteredService(), atRequest.getRegisteredService());
        assertEquals(holder.getClientId(), atRequest.getClientId());
        assertEquals(holder.getUserProfile().getId(), atRequest.getUserProfile().getId());
    }

    @Test
    void verifyCompactedRequestKeepsWhatAuthorizationNeeds() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val authentication = RegisteredServiceTestUtils.getAuthentication(registeredService.getClientId(),
            Map.<String, List<Object>>of(OAuth20Constants.NONCE, List.of("n-0S6_WzA2Mj"), OAuth20Constants.STATE, List.of("af0ifjsldkj")));
        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(webApplicationServiceFactory.createService(registeredService.getClientId()))
            .authentication(authentication)
            .registeredService(registeredService)
            .redirectUri("https://oauth.example.org/callback")
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .responseMode(OAuth20ResponseModeTypes.QUERY)
            .scopes(new LinkedHashSet<>(List.of("openid", "profile")))
            .codeChallenge("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
            .codeChallengeMethod("S256")
            .claims(Map.of("userinfo", Map.of("email", Map.of("essential", true))))
            .build();
        holder.getParameters().put(OAuth20Constants.STATE, "af0ifjsldkj");
        holder.getParameters().put(OAuth20Constants.CLIENT_SECRET, "secret");
        val factory = (OidcPushedAuthorizationRequestFactory) defaultTicketFactory.get(OidcPushedAuthorizationRequest.class);
        val ticket = factory.create(holder);

        val compactor = new OidcPushedAuthorizationRequestCompactor(new DirectObjectProvider<>(defaultTicketFactory),
            webApplicationServiceFactory, PrincipalFactoryUtils.newPrincipalFactory(), servicesManager);
        val expanded = (OidcPushedAuthorizationRequest) compactor.expand(compactor.compact(ticket));
        assertEquals(ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond(),
            expanded.getExpirationPolicy().toMaximumExpirationTime(expanded).toEpochSecond());

        val request = factory.toAccessTokenRequest(expanded);
        assertEquals(registeredService.getClientId(), request.getClientId());
        assertEquals(registeredService.getId(), request.getRegisteredService().getId());
        assertEquals(registeredService.getClientId(), request.getService().getId());
        assertEquals(holder.getRedirectUri(), request.getRedirectUri());
        assertEquals(OAuth20ResponseModeTypes.QUERY, request.getResponseMode());
        assertEquals(holder.getScopes(), request.getScopes());
        assertEquals(holder.getCodeChallenge(), request.getCodeChallenge());
        assertEquals("S256", request.getCodeChallengeMethod());
        assertEquals(Map.of("essential", true), request.getClaims().get("userinfo").get("email"));
        assertEquals("af0ifjsldkj", request.getParameters().get(OAuth20Constants.STATE));
        assertFalse(request.getParameters().containsKey(OAuth20Constants.CLIENT_SECRET));
        assertEquals(registeredService.getClientId(), request.getAuthentication().getPrincipal().getId());
        assertEquals(List.of("n-0S6_WzA2Mj"), request.getAuthentication().getAttributes().get(OAuth20Constants.NONCE));
    }
}
