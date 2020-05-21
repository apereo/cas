package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenAtHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
@TestPropertySource(properties = {
    "cas.authn.oauth.accessToken.crypto.encryption-enabled=false",
    "cas.authn.oauth.accessToken.createAsJwt=true"
})
@TestPropertySource(properties = "cas.authn.oauth.accessToken.crypto.encryption-enabled=false")
public class OidcIdTokenGeneratorServiceTests extends AbstractOidcTests {

    private static final String OIDC_CLAIM_EMAIL = "email";
    private static final String OIDC_CLAIM_PHONE_NUMBER = "phone_number";
    private static final String OIDC_CLAIM_NAME = "name";

    @Autowired
    @Qualifier("refreshScope")
    private RefreshScope refreshScope;

    @ParameterizedTest
    @ValueSource(strings = {
        "classpath:keystore.jwks",
        "classpath:keystore-p256.jwks",
        "classpath:keystore-p384.jwks",
        "classpath:keystore-p521.jwks"
    })
    public void verifyTokenGeneration(final String jwks) throws Exception {
        setJwksFile(jwks);
        refreshScope.refresh("oidcJsonWebKeystoreGeneratorService");
        refreshScope.refresh("oidcDefaultJsonWebKeystoreCacheLoader");
        refreshScope.refresh("oidcDefaultJsonWebKeystoreCache");
        refreshScope.refresh("oidcTokenSigningAndEncryptionService");

        val request = new MockHttpServletRequest();
        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId("casuser");

        request.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val response = new MockHttpServletResponse();

        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TGT_ID);
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

        val accessToken = mock(OAuth20AccessToken.class);
        val expirationPolicy = mock(ExpirationPolicy.class);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid");
        when(expirationPolicy.getTimeToLive()).thenReturn(Long.valueOf(3600));
        when(accessToken.getExpirationPolicy()).thenReturn(expirationPolicy);
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getId()).thenReturn(getClass().getSimpleName());
        when(accessToken.getScopes()).thenReturn(Set.of(OPENID.getScope(), PROFILE.getScope(), EMAIL.getScope()));
        when(accessToken.getService()).thenReturn(service);

        OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(accessToken.getService())
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .encode();
        val idToken = oidcIdTokenGenerator.generate(request, response, accessToken, 30,
            OAuth20ResponseTypes.CODE, registeredService);
        assertNotNull(idToken);

        val claims = oidcTokenSigningAndEncryptionService.decode(idToken, Optional.ofNullable(registeredService));
        assertNotNull(claims);
        assertTrue(claims.hasClaim(OIDC_CLAIM_EMAIL));
        assertTrue(claims.hasClaim(OidcConstants.CLAIM_AUTH_TIME));
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
        request.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val response = new MockHttpServletResponse();

        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TGT_ID);

        when(tgt.getServices()).thenReturn(new HashMap<>());
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser",
            CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                OAuth20Constants.NONCE, List.of("some-nonce")));
        when(tgt.getAuthentication()).thenReturn(authentication);

        val accessToken = mock(OAuth20AccessToken.class);
        val expirationPolicy = mock(ExpirationPolicy.class);
        val service = new WebApplicationServiceFactory().createService("mockService");
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid");
        when(expirationPolicy.getTimeToLive()).thenReturn(Long.valueOf(3600));
        when(accessToken.getExpirationPolicy()).thenReturn(expirationPolicy);
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getId()).thenReturn(getClass().getSimpleName());
        when(accessToken.getService()).thenReturn(service);

        OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(accessToken.getService())
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .encode();
        val idToken = oidcIdTokenGenerator.generate(request, response, accessToken, 30,
            OAuth20ResponseTypes.CODE, OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid"));
        assertNotNull(idToken);
    }

    @Test
    public void verifyTokenGenerationFailsWithoutProfile() {
        assertThrows(IllegalArgumentException.class, () -> {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val accessToken = mock(OAuth20AccessToken.class);
            val expirationPolicy = mock(ExpirationPolicy.class);
            val service = new WebApplicationServiceFactory().createService("mockService");
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid");
            var authentication = CoreAuthenticationTestUtils.getAuthentication("casuser",
                CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                    OAuth20Constants.NONCE, List.of("some-nonce")));
            when(expirationPolicy.getTimeToLive()).thenReturn(Long.valueOf(3600));
            when(accessToken.getExpirationPolicy()).thenReturn(expirationPolicy);
            when(accessToken.getAuthentication()).thenReturn(authentication);
            when(accessToken.getId()).thenReturn(getClass().getSimpleName());
            when(accessToken.getService()).thenReturn(service);

            OAuth20JwtAccessTokenEncoder.builder()
                .accessToken(accessToken)
                .registeredService(registeredService)
                .service(accessToken.getService())
                .casProperties(casProperties)
                .accessTokenJwtBuilder(accessTokenJwtBuilder)
                .build()
                .encode();
            oidcIdTokenGenerator.generate(request, response, accessToken, 30,
                OAuth20ResponseTypes.CODE,
                OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid"));
        });
    }

    @AllArgsConstructor
    private static class VerifyAccessTokenAsJwtArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                Arguments.of("classpath:keystore.jwks", AlgorithmIdentifiers.RSA_USING_SHA256, Boolean.TRUE),
                Arguments.of("classpath:keystore.jwks", AlgorithmIdentifiers.RSA_USING_SHA256, Boolean.FALSE),
                Arguments.of("classpath:keystore-p256.jwks", AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256, Boolean.TRUE),
                Arguments.of("classpath:keystore-p256.jwks", AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256, Boolean.FALSE),
                Arguments.of("classpath:keystore-p384.jwks", AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384, Boolean.TRUE),
                Arguments.of("classpath:keystore-p384.jwks", AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384, Boolean.FALSE),
                Arguments.of("classpath:keystore-p521.jwks", AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512, Boolean.TRUE),
                Arguments.of("classpath:keystore-p521.jwks", AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512, Boolean.FALSE)
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(VerifyAccessTokenAsJwtArgumentsProvider.class)
    public void verifyAccessTokenAsJwt(final String jwks, final String algorithm, final Boolean signEnabled) throws Exception {
        setJwksFile(jwks);
        refreshScope.refresh("oidcJsonWebKeystoreGeneratorService");
        refreshScope.refresh("oidcDefaultJsonWebKeystoreCacheLoader");
        refreshScope.refresh("oidcDefaultJsonWebKeystoreCache");
        refreshScope.refresh("oidcTokenSigningAndEncryptionService");

        val encKey = oidcDefaultJsonWebKeystoreCache.get(StringUtils.EMPTY).get();
        val request = new MockHttpServletRequest();
        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId("casuser");
        request.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val response = new MockHttpServletResponse();

        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getServices()).thenReturn(new HashMap<>());
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser",
            CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                OAuth20Constants.NONCE, List.of("some-nonce")));
        when(tgt.getAuthentication()).thenReturn(authentication);

        val accessToken = getAccessToken();
        val registeredService = getOidcRegisteredService(accessToken.getClientId());
        if (encKey instanceof RsaJsonWebKey) {
            registeredService.setIdTokenEncryptionAlg(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        } else if (encKey instanceof EllipticCurveJsonWebKey) {
            registeredService.setIdTokenEncryptionAlg(KeyManagementAlgorithmIdentifiers.ECDH_ES_A256KW);
        } else {
            throw new IllegalArgumentException("Unsupported public key format");
        }
        registeredService.setJwtAccessToken(true);
        registeredService.setIdTokenSigningAlg(algorithm);
        
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("false"),
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty(signEnabled.toString())
        ));
        
        this.servicesManager.save(registeredService);
        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(accessToken.getService())
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .encode();
        val idToken = oidcIdTokenGenerator.generate(request, response,
            accessToken, 30, OAuth20ResponseTypes.CODE, registeredService);
        assertNotNull(idToken);
        val claims = oidcTokenSigningAndEncryptionService.decode(idToken, Optional.of(registeredService));
        assertNotNull(claims);
        assertTrue(claims.hasClaim(OidcConstants.CLAIM_AT_HASH));
        assertTrue(claims.hasClaim(OidcConstants.CLAIM_AUTH_TIME));
        val hash = claims.getClaimValue(OidcConstants.CLAIM_AT_HASH, String.class);
        val newHash = OAuth20AccessTokenAtHashGenerator.builder()
            .encodedAccessToken(encodedAccessToken)
            .registeredService(registeredService)
            .algorithm(registeredService.getIdTokenSigningAlg())
            .build()
            .generate();
        assertEquals(hash, newHash);
    }
}
