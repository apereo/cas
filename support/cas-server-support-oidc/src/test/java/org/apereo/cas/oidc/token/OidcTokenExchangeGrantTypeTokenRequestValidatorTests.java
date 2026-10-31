package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcTokenExchangeGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDC")
class OidcTokenExchangeGrantTypeTokenRequestValidatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oauthTokenExchangeGrantTypeTokenRequestValidator")
    private OAuth20TokenRequestValidator validator;


    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        response = new MockHttpServletResponse();
        val profile = new DefaultOAuth20UserProfile("casuser");
        storeProfileIntoSession(request, profile);
    }

    @Test
    void verifySubjectTokenAsIdToken() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString()).setEncryptIdToken(false);
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE.getType()));
        servicesManager.save(registeredService);

        val profile = new DefaultOAuth20UserProfile(registeredService.getClientId());
        profile.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        storeProfileIntoSession(request, profile);

        request.addParameter(OAuth20Constants.AUDIENCE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ID_TOKEN.getType());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());

        val accessToken = getAccessToken(registeredService.getClientId());
        when(accessToken.getScopes()).thenReturn(
            Set.of(OidcConstants.StandardScopes.DEVICE_SSO.getScope(),
                OidcConstants.StandardScopes.PROFILE.getScope(),
                OidcConstants.StandardScopes.OPENID.getScope()));

        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(accessToken)
            .userProfile(profile)
            .responseType(OAuth20ResponseTypes.CODE)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .registeredService(registeredService)
            .build();
        val idToken = oidcIdTokenGenerator.generate(idTokenContext);
        request.setParameter(OAuth20Constants.SUBJECT_TOKEN, idToken.token());
        assertTrue(validator.validate(request, response));
    }
}
