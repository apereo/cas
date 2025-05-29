package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcAccessTokenTokenExchangeGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDC")
class OidcAccessTokenTokenExchangeGrantRequestExtractorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("accessTokenTokenExchangeGrantRequestExtractor")
    private AccessTokenGrantRequestExtractor extractor;

    @Test
    void verifyExtractionWithJwtType() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString()).setEncryptIdToken(false);
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE.getType()));
        servicesManager.save(registeredService);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val userProfile = new CommonProfile();
        userProfile.setId("casuser");
        userProfile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());
        val profileManager = new ProfileManager(context, oauthDistributedSessionStore);
        profileManager.save(true, userProfile, false);

        val accessToken = getAccessToken(registeredService.getClientId());
        when(accessToken.getScopes()).thenReturn(
            Set.of(OidcConstants.StandardScopes.DEVICE_SSO.getScope(),
                OidcConstants.StandardScopes.PROFILE.getScope(),
                OidcConstants.StandardScopes.OPENID.getScope()));
        ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
        ticketRegistry.addTicket(accessToken);

        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(accessToken)
            .userProfile(profileManager.getProfile().orElseThrow())
            .responseType(OAuth20ResponseTypes.CODE)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .registeredService(registeredService)
            .build();
        val idToken = oidcIdTokenGenerator.generate(idTokenContext);

        request.addParameter(OAuth20Constants.SUBJECT_TOKEN, idToken.token());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ID_TOKEN.getType());

        request.addParameter(OAuth20Constants.ACTOR_TOKEN, idToken.deviceSecret());
        request.addParameter(OAuth20Constants.ACTOR_TOKEN_TYPE, OAuth20TokenExchangeTypes.DEVICE_SECRET.getType());

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.AUDIENCE, registeredService.getClientId());
        
        val tokenContext = extractor.extract(context);
        assertNotNull(tokenContext);
    }
}
