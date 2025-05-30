package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenResponseEncoder;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.token.JwtBuilder;
import com.nimbusds.jose.JWSHeader;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIdTokenSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
class OidcIdTokenSigningAndEncryptionServiceTests {
    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.jwks.file-system.jwks-file=classpath:multiple-keys.jwks")
    class KeystoreWithMultipleKeysTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val claims = getClaims();

            val es512 = getOidcRegisteredService("ES512");
            es512.setIdTokenSigningAlg("ES512");
            es512.setJwksKeyId("EC");
            es512.setEncryptIdToken(false);

            val rs256 = getOidcRegisteredService("RS256");
            rs256.setIdTokenSigningAlg("RS256");
            rs256.setJwksKeyId("RSA");
            rs256.setEncryptIdToken(false);

            val result1 = oidcTokenSigningAndEncryptionService.encode(es512, claims);
            assertNotNull(result1);

            val result2 = oidcTokenSigningAndEncryptionService.encode(rs256, claims);
            assertNotNull(result2);

            val result3 = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), claims);
            assertNotNull(result3);
        }

        @Test
        void verifyJwtAccessTokenOperation() throws Throwable {
            val registeredService = getOidcRegisteredService("ES512");
            registeredService.setIdTokenSigningAlg(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
            registeredService.setJwksKeyId("EC");
            registeredService.setJwtAccessToken(true);
            registeredService.setJwtAccessTokenSigningAlg(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
            registeredService.setEncryptIdToken(false);

            val accessTokenContext = AccessTokenRequestContext.builder()
                .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
                .responseType(OAuth20ResponseTypes.NONE)
                .registeredService(registeredService)
                .generateRefreshToken(true)
                .service(RegisteredServiceTestUtils.getService(registeredService.getServiceId()))
                .authentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()))
                .scopes(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
                .build();
            val accessTokenResult = oauthTokenGenerator.generate(accessTokenContext);
            assertNotNull(accessTokenResult);

            val encodedModelAndView = new OAuth20AccessTokenResponseEncoder(oidcConfigurationContext)
                .encode(accessTokenContext, accessTokenResult);
            assertNotNull(encodedModelAndView);
            val accessToken = (String) encodedModelAndView.getModel().get(OAuth20Constants.ACCESS_TOKEN);
            val header = (JWSHeader) JwtBuilder.parseHeader(accessToken);
            assertNotNull(header);
            assertEquals(registeredService.getJwtAccessTokenSigningAlg(), header.getAlgorithm().getName());
            assertEquals(registeredService.getJwksKeyId(), header.getKeyID());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.discovery.id-token-signing-alg-values-supported=RS256,RS384,RS512",
        "cas.authn.oidc.discovery.id-token-encryption-encoding-values-supported=A128CBC-HS256,A192CBC-HS384,A256CBC-HS512,A128GCM,A192GCM,A256GCM"
    })
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val claims = getClaims();
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()));
            val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            assertNotNull(result);
        }

        @Test
        void verifyEncryptionOptional() throws Throwable {
            val claims = getClaims();
            val service = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()));
            service.setJwks(null);
            service.setEncryptIdToken(true);
            service.setIdTokenEncryptionOptional(true);
            val result = oidcTokenSigningAndEncryptionService.encode(service, claims);
            assertNotNull(result);
        }

        @Test
        void verifyWrongType() {
            assertFalse(oidcTokenSigningAndEncryptionService.shouldEncryptToken(getOAuthRegisteredService("1", "http://localhost/cas")));
            assertFalse(oidcTokenSigningAndEncryptionService.shouldSignToken(getOAuthRegisteredService("1", "http://localhost/cas")));
        }

        @Test
        void verifySkipSigning() {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()), false, false);
            val result = oidcTokenSigningAndEncryptionService.shouldSignToken(oidcRegisteredService);
            assertFalse(result);
        }

        @Test
        void verifyValidationOperation() throws Throwable {
            val claims = getClaims();
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()), true, false);
            val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            val jwt = oidcTokenSigningAndEncryptionService.decode(result, Optional.of(oidcRegisteredService));
            assertNotNull(jwt);
        }

        @Test
        void verifyDecodingFailureBadToken() {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()), true, false);
            assertThrows(IllegalArgumentException.class,
                () -> oidcTokenSigningAndEncryptionService.decode("bad-token", Optional.of(oidcRegisteredService)));
        }

        @Test
        void verifyDecodingFailureNoIssuer() throws Throwable {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()), true, false);
            val claims = getClaims();
            claims.setIssuer(StringUtils.EMPTY);
            val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            assertThrows(IllegalArgumentException.class,
                () -> oidcTokenSigningAndEncryptionService.decode(result, Optional.of(oidcRegisteredService)));
        }

        @Test
        void verifyDecodingFailureBadIssuer() throws Throwable {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()), true, false);
            val claims = getClaims();
            claims.setIssuer("bad-issuer");
            val result2 = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            assertThrows(IllegalArgumentException.class,
                () -> oidcTokenSigningAndEncryptionService.decode(result2, Optional.of(oidcRegisteredService)));
        }

        @Test
        void verifyDecodingFailureBadClient() throws Throwable {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()), true, false);
            val claims = getClaims();
            claims.setStringClaim(OAuth20Constants.CLIENT_ID, StringUtils.EMPTY);
            val result3 = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            assertThrows(IllegalArgumentException.class,
                () -> oidcTokenSigningAndEncryptionService.decode(result3, Optional.of(oidcRegisteredService)));
        }

        @Test
        void verifyNoneNotSupported() {
            val claims = getClaims();
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()));
            oidcRegisteredService.setIdTokenSigningAlg(AlgorithmIdentifiers.NONE);
            assertThrows(IllegalArgumentException.class, () -> oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims));
            oidcRegisteredService.setIdTokenSigningAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
            oidcRegisteredService.setIdTokenEncryptionAlg(AlgorithmIdentifiers.NONE);
            assertThrows(IllegalArgumentException.class, () -> oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.discovery.id-token-signing-alg-values-supported=none",
        "cas.authn.oidc.discovery.id-token-encryption-encoding-values-supported=none"
    })
    class NoneTests extends AbstractOidcTests {
        @Test
        void verifyNoneSupported() throws Throwable {
            val claims = getClaims();
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(),
                "https://example-%s.org".formatted(UUID.randomUUID().toString()));
            oidcRegisteredService.setIdTokenSigningAlg(AlgorithmIdentifiers.NONE);
            oidcRegisteredService.setIdTokenEncryptionAlg(AlgorithmIdentifiers.NONE);
            val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            assertNotNull(result);
        }
    }
}
