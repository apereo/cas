package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcTokenAuthorizationResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDC")
class OidcTokenAuthorizationResponseBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcTokenResponseBuilder")
    private OAuth20AuthorizationResponseBuilder oidcTokenResponseBuilder;

    @Test
    void verifyAccessTokenNamesOidcIssuer() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setJwtAccessToken(true);
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
            .responseType(OAuth20ResponseTypes.TOKEN)
            .userProfile(profile)
            .redirectUri("https://oauth.example.org")
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .scopes(code.getScopes())
            .build();

        servicesManager.save(registeredService);
        val mv = oidcTokenResponseBuilder.build(holder);
        val fragment = new URIBuilder(((AbstractUrlBasedView) mv.getView()).getUrl()).getFragment();
        val accessToken = Arrays.stream(fragment.split("&"))
            .map(parameter -> parameter.split("=", 2))
            .filter(parameter -> OAuth20Constants.ACCESS_TOKEN.equals(parameter[0]))
            .map(parameter -> parameter[1])
            .findFirst()
            .orElseThrow();
        val expectedIssuer = oidcIssuerService.determineIssuer(Optional.of(registeredService));
        assertEquals(expectedIssuer, JWTParser.parse(accessToken).getJWTClaimsSet().getIssuer());
        assertNotEquals(casProperties.getServer().getPrefix(), expectedIssuer);
    }
}
