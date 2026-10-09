package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
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

    @Test
    void verifyAccessTokenHashMatchesReturnedAccessToken() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setJwtAccessToken(true);
        registeredService.setIdTokenSigningAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
        val code = addCode(principal, registeredService);

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");

        val holder = AccessTokenRequestContext.builder()
            .token(code)
            .clientId(registeredService.getClientId())
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication(principal))
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.IDTOKEN_TOKEN)
            .userProfile(profile)
            .redirectUri("https://oauth.example.org")
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .scopes(code.getScopes())
            .build();

        servicesManager.save(registeredService);
        val mv = oidcImplicitIdTokenAndTokenCallbackUrlBuilder.build(holder);
        val fragment = new URIBuilder(((AbstractUrlBasedView) mv.getView()).getUrl()).getFragment();
        val parameters = Arrays.stream(fragment.split("&"))
            .map(parameter -> parameter.split("=", 2))
            .collect(Collectors.toMap(parameter -> parameter[0], parameter -> parameter[1]));
        val accessToken = parameters.get(OAuth20Constants.ACCESS_TOKEN);
        assertNotNull(accessToken);
        val idToken = parameters.get(OidcConstants.ID_TOKEN);
        assertNotNull(idToken);

        val claims = oidcTokenSigningAndEncryptionService.decode(idToken, Optional.of(registeredService));
        val expectedHash = OAuth20TokenHashGenerator.builder()
            .token(accessToken)
            .registeredService(registeredService)
            .algorithm(registeredService.getIdTokenSigningAlg())
            .build()
            .generate();
        assertEquals(expectedHash, claims.getClaimValue(OidcConstants.CLAIM_AT_HASH, String.class));
    }
}
