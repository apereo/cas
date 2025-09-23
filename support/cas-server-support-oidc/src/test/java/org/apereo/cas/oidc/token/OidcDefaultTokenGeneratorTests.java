package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultTokenGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
class OidcDefaultTokenGeneratorTests extends AbstractOidcTests {

    @Test
    void verifyCibaAuthRequestClaim() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        registeredService.setBackchannelClientNotificationEndpoint("https://localhost:1234");
        servicesManager.save(registeredService);

        val cibaRequest = newCibaRequest(registeredService, RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString()));
        val accessTokenContext = AccessTokenRequestContext.builder()
            .grantType(OAuth20GrantTypes.CIBA)
            .responseType(OAuth20ResponseTypes.NONE)
            .registeredService(registeredService)
            .generateRefreshToken(true)
            .cibaRequestId(cibaRequest.getEncodedId())
            .authentication(cibaRequest.getAuthentication())
            .service(RegisteredServiceTestUtils.getService())
            .scopes(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .build();
        val result = oauthTokenGenerator.generate(accessTokenContext);
        assertNotNull(result);
        val accessToken = (OAuth20AccessToken) result.getAccessToken().orElseThrow();
        assertEquals(cibaRequest.getEncodedId(), accessToken.getAuthentication().getSingleValuedAttribute(OidcConstants.AUTH_REQ_ID).toString());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(registeredService.getClientId());
        profile.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(accessToken)
            .userProfile(profile)
            .responseType(accessTokenContext.getResponseType())
            .grantType(accessTokenContext.getGrantType())
            .registeredService(registeredService)
            .refreshToken((OAuth20RefreshToken) result.getRefreshToken().orElseThrow())
            .build();
        
        val oidcIdToken = oidcIdTokenGenerator.generate(idTokenContext);
        assertNotNull(oidcIdToken);
        assertEquals(cibaRequest.getEncodedId(), oidcIdToken.claims().getStringClaimValue(OidcConstants.CLAIM_AUTH_REQ_ID));
        assertTrue(oidcIdToken.claims().hasClaim(OidcConstants.CLAIM_AT_HASH));
        assertTrue(oidcIdToken.claims().hasClaim(OidcConstants.CLAIM_RT_HASH));
    }
}
