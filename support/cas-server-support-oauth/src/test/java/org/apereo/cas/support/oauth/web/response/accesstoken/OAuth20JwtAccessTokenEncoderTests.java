package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20JwtAccessTokenEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuthToken")
class OAuth20JwtAccessTokenEncoderTests {

    @Nested
    class CipherEnabled extends AbstractOAuth20Tests {
        @Test
        void verifyAccessTokenIdEncodingAsJwtWithTokenResult() throws Throwable {
            val accessToken = getAccessToken();
            val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
            val tokenResult = OAuth20AccessTokenResponseResult
                .builder()
                .registeredService(registeredService)
                .service(accessToken.getService())
                .grantType(OAuth20GrantTypes.TOKEN_EXCHANGE)
                .requestedTokenType(OAuth20TokenExchangeTypes.JWT)
                .build();
            var encoder = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, tokenResult, accessToken, UUID.randomUUID().toString());
            var encodedAccessToken = encoder.encode(accessToken.getId());
            assertNotNull(encodedAccessToken);
            var decoded = OAuth20JwtAccessTokenEncoder.toDecodableCipher(configurationContext.getAccessTokenJwtBuilder()).decode(encodedAccessToken);
            assertNotNull(decoded);
            assertEquals(accessToken.getId(), decoded);
            
            encoder = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, tokenResult, accessToken);
            encodedAccessToken = encoder.encode(accessToken.getId());
            decoded = OAuth20JwtAccessTokenEncoder.toDecodableCipher(configurationContext.getAccessTokenJwtBuilder()).decode(encodedAccessToken);
            assertEquals(accessToken.getId(), decoded);
        }
        
        @Test
        void verifyAccessTokenIdEncodingAsJwtWithoutServiceKeys() throws Throwable {
            val accessToken = getAccessToken();
            val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
            val encoder = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, registeredService, accessToken);
            val encodedAccessToken = encoder.encode(accessToken.getId());
            assertNotNull(encodedAccessToken);
            val decoded = OAuth20JwtAccessTokenEncoder.toDecodableCipher(configurationContext.getAccessTokenJwtBuilder()).decode(encodedAccessToken);
            assertNotNull(decoded);
            assertEquals(accessToken.getId(), decoded);
        }

        @Test
        void verifyAccessTokenIdEncodingAsJwtWithServiceKeys() throws Throwable {
            val accessToken = getAccessToken();
            val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
            val encoder = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, registeredService, accessToken);
            val encodedAccessToken = encoder.encode(accessToken.getId());
            assertNotNull(encodedAccessToken);
            val decoded = OAuth20JwtAccessTokenEncoder.toDecodableCipher(configurationContext.getAccessTokenJwtBuilder()).decode(encodedAccessToken);
            assertNotNull(decoded);
            assertEquals(accessToken.getId(), decoded);
        }

        private OAuthRegisteredService getRegisteredServiceForJwtAccessTokenWithoutKeys(final OAuth20AccessToken accessToken) {
            val registeredService = getRegisteredService(accessToken.getService().getId(), "secret", new LinkedHashSet<>());
            registeredService.setJwtAccessToken(true);
            servicesManager.save(registeredService);
            return registeredService;
        }

        private OAuthRegisteredService getRegisteredServiceForJwtAccessTokenWithKeys(final OAuth20AccessToken accessToken) {
            val registeredService = getRegisteredService(accessToken.getService().getId(), "secret", new LinkedHashSet<>());
            registeredService.setProperties(Map.of(
                RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ALG.getPropertyName(),
                new DefaultRegisteredServiceProperty(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256),
                RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
                new DefaultRegisteredServiceProperty("true"),
                RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED.getPropertyName(),
                new DefaultRegisteredServiceProperty("true"),
                RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY.getPropertyName(),
                new DefaultRegisteredServiceProperty("1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM"),
                RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_KEY.getPropertyName(),
                new DefaultRegisteredServiceProperty("szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w")
            ));
            registeredService.setJwtAccessToken(true);
            servicesManager.save(registeredService);
            return registeredService;
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oauth.access-token.crypto.enabled=false")
    class CipherDisabled extends AbstractOAuth20Tests {

        private OAuthRegisteredService getRegisteredServiceForJwtAccessTokenWithoutKeys(final OAuth20AccessToken accessToken) {
            val registeredService = getRegisteredService(accessToken.getService().getId(), "secret", new LinkedHashSet<>());
            registeredService.setJwtAccessToken(true);
            servicesManager.save(registeredService);
            return registeredService;
        }
        
        @Test
        void verifyAccessTokenIdEncodingWithJwtWithNoCipher() throws Throwable {
            val accessToken = getAccessToken();
            val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
            val encodedAccessToken1 = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, registeredService, accessToken).encode(accessToken.getId());
            assertNotNull(encodedAccessToken1);
            val encodedAccessToken2 = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, registeredService, accessToken).encode(accessToken.getId());
            assertEquals(encodedAccessToken1, encodedAccessToken2);
        }
    }

}
