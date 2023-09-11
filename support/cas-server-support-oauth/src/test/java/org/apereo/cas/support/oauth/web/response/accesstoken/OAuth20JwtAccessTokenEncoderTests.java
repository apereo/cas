package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenCipherExecutor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20JwtAccessTokenEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuthToken")
class OAuth20JwtAccessTokenEncoderTests extends AbstractOAuth20Tests {
    @Test
    void verifyAccessTokenHeaderService() throws Throwable {
        val accessToken = getAccessToken();
        val builder = getCipherDisabledJwtBuilder();

        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setId(100200);
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        var encoder = getAccessTokenEncoder(accessToken, builder, registeredService);
        val encodedAccessToken = encoder.encode(accessToken.getId());
        assertNotNull(encodedAccessToken);

        encoder = getAccessTokenEncoder(accessToken, builder, null);
        encoder.decode(encodedAccessToken);

    }

    @Test
    void verifyAccessTokenIdEncodingWithoutJwt() throws Throwable {
        val accessToken = getAccessToken();

        val builder = getCipherDisabledJwtBuilder();

        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        val encodedAccessToken1 = getAccessTokenEncoder(accessToken, builder, registeredService).encode(accessToken.getId());
        assertNotNull(encodedAccessToken1);

        val encodedAccessToken2 = getAccessTokenEncoder(accessToken, builder, registeredService).encode(accessToken.getId());
        assertEquals(encodedAccessToken1, encodedAccessToken2);
    }

    @Test
    void verifyAccessTokenIdEncodingWithJwtWithNoCipher() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
        val builder = getCipherDisabledJwtBuilder();

        val encodedAccessToken1 = getAccessTokenEncoder(accessToken, builder, registeredService).encode(accessToken.getId());
        assertNotNull(encodedAccessToken1);

        val encodedAccessToken2 = getAccessTokenEncoder(accessToken, builder, registeredService).encode(accessToken.getId());
        assertEquals(encodedAccessToken1, encodedAccessToken2);
    }

    @Test
    void verifyAccessTokenIdEncodingWithJwtGlobally() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);
        val encodedAccessToken = encoder.encode(accessToken.getId());
        assertNotNull(encodedAccessToken);

        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    void verifyExtractionAsParameterForService() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode(accessToken.getId());
        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    void verifyExtractionAsParameter() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode(accessToken.getId());
        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    void verifyAccessTokenIdEncodingWithJwtForService() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode(accessToken.getId());
        assertNotNull(encodedAccessToken);

        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    void verifyAccessTokenIdEncodingWithJwt() throws Throwable {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode(accessToken.getId());
        assertNotNull(encodedAccessToken);

        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    private OAuth20JwtAccessTokenEncoder getAccessTokenEncoder(final OAuth20AccessToken accessToken,
                                                               final OAuth20JwtBuilder builder,
                                                               final RegisteredService registeredService) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(builder)
            .casProperties(casProperties)
            .build();
    }

    private OAuth20JwtBuilder getCipherDisabledJwtBuilder() {
        return new OAuth20JwtBuilder(
            CipherExecutor.noOp(),
            servicesManager,
            RegisteredServiceCipherExecutor.noOp(),
            casProperties);
    }

    private OAuthRegisteredService getRegisteredServiceForJwtAccessTokenWithoutKeys(final OAuth20AccessToken accessToken) {
        val registeredService = getRegisteredService(accessToken.getService().getId(), "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);
        return registeredService;
    }

    private OAuth20JwtBuilder getCipherEnabledJwtBuilder() {
        return new OAuth20JwtBuilder(
            new OAuth20JwtAccessTokenCipherExecutor(true, true),
            servicesManager,
            new OAuth20RegisteredServiceJwtAccessTokenCipherExecutor(), casProperties);
    }

    private OAuthRegisteredService getRegisteredServiceForJwtAccessTokenWithKeys(final OAuth20AccessToken accessToken) {
        val registeredService = getRegisteredService(accessToken.getService().getId(), "secret", new LinkedHashSet<>());
        registeredService.setProperties(Map.of(
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
