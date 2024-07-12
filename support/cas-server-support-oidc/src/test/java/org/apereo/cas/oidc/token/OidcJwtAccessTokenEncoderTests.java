package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.crypto.DecodableCipher;
import org.apereo.cas.util.crypto.EncodableCipher;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This is {@link OidcJwtAccessTokenEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oauth.access-token.crypto.encryption-enabled=false")
class OidcJwtAccessTokenEncoderTests extends AbstractOidcTests {
    private EncodableCipher<String, String> getAccessTokenEncodingCipher(final OAuth20AccessToken accessToken,
                                                                         final RegisteredService registeredService) {
        return OAuth20JwtAccessTokenEncoder.toEncodableCipher(oidcConfigurationContext,
            registeredService, accessToken, accessToken.getService(), false);
    }

    private DecodableCipher<String, String> getAccessTokenDecodingCipher(final RegisteredService registeredService) {
        return OAuth20JwtAccessTokenEncoder.toDecodableCipher(oidcAccessTokenJwtBuilder, registeredService);
    }

    @Test
    void verifyEncodingWithoutEncryptionForService() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getOidcRegisteredService(accessToken.getClientId());
        registeredService.setJwtAccessToken(true);
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("false")
        ));
        this.servicesManager.save(registeredService);

        val token1 = getAccessTokenEncodingCipher(accessToken, registeredService).encode(accessToken.getId());
        val token2 = getAccessTokenEncodingCipher(accessToken, registeredService).encode(accessToken.getId());
        assertEquals(token1, token2);
    }

    @Test
    void verifyExtractionAsParameterForService() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val encoder = getAccessTokenEncodingCipher(accessToken, registeredService);
        val encodedAccessToken = encoder.encode(accessToken.getId());

        val decoded = getAccessTokenDecodingCipher(registeredService).decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    public static Stream<Arguments> getNonTokenArgs() {
        return Stream.of(
            arguments((String) null),
            arguments(StringUtils.EMPTY),
            arguments("nodotintoken"),
            arguments("bad.token.id")
        );
    }

    @ParameterizedTest
    @MethodSource("getNonTokenArgs")
    void verifyNonEncodedToken(final String tokenId) throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val encoder = getAccessTokenDecodingCipher(registeredService);
        val decode = encoder.decode(tokenId);
        assertEquals(tokenId, decode);
    }

    @Test
    void verifyEncodingWithNoCiphersForService() throws Throwable {
        val accessToken = getAccessToken(StringUtils.EMPTY, "encoding-service-clientid");
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);

        val encoder = getAccessTokenEncodingCipher(accessToken, registeredService);
        val token1 = encoder.encode(accessToken.getId());
        val token2 = encoder.encode(accessToken.getId());
        assertEquals(token1, token2);

        val decodingCipher = getAccessTokenDecodingCipher(registeredService);
        val decoded1 = decodingCipher.decode(token1);
        val decoded2 = decodingCipher.decode(token2);
        assertEquals(decoded1, decoded2);
        assertEquals(accessToken.getId(), decoded1);
    }

    private OidcRegisteredService getRegisteredServiceForJwtAccessTokenWithKeys(final OAuth20AccessToken accessToken) {
        val registeredService = getOidcRegisteredService(accessToken.getClientId());
        registeredService.setJwtAccessToken(true);

        val property = new DefaultRegisteredServiceProperty("false");
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            property,
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED.getPropertyName(),
            property
        ));
        this.servicesManager.save(registeredService);
        return registeredService;
    }
}
