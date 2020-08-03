package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
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
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20JwtAccessTokenEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class OAuth20JwtAccessTokenEncoderTests extends AbstractOAuth20Tests {
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

    private static OAuth20JwtBuilder getCipherDisabledJwtBuilder() {
        return new OAuth20JwtBuilder("http://cas.example.org/prefix",
            CipherExecutor.noOp(),
            mock(ServicesManager.class),
            RegisteredServiceCipherExecutor.noOp());
    }

    @Test
    public void verifyAccessTokenIdEncodingWithoutJwt() {
        val accessToken = getAccessToken();

        val builder = getCipherDisabledJwtBuilder();

        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        val encodedAccessToken1 = getAccessTokenEncoder(accessToken, builder, registeredService).encode();
        assertNotNull(encodedAccessToken1);

        val encodedAccessToken2 = getAccessTokenEncoder(accessToken, builder, registeredService).encode();
        assertEquals(encodedAccessToken1, encodedAccessToken2);
    }

    @Test
    public void verifyAccessTokenIdEncodingWithJwtWithNoCipher() {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
        val builder = getCipherDisabledJwtBuilder();

        val encodedAccessToken1 = getAccessTokenEncoder(accessToken, builder, registeredService).encode();
        assertNotNull(encodedAccessToken1);

        val encodedAccessToken2 = getAccessTokenEncoder(accessToken, builder, registeredService).encode();
        assertEquals(encodedAccessToken1, encodedAccessToken2);
    }

    private OAuthRegisteredService getRegisteredServiceForJwtAccessTokenWithoutKeys(final OAuth20AccessToken accessToken) {
        val registeredService = getRegisteredService(accessToken.getService().getId(), "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);
        return registeredService;
    }

    @Test
    public void verifyAccessTokenIdEncodingWithJwtGlobally() {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);
        val encodedAccessToken = encoder.encode();
        assertNotNull(encodedAccessToken);

        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    public void verifyExtractionAsParameterForService() {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode();
        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    public void verifyExtractionAsParameter() {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithoutKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode();
        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    public void verifyAccessTokenIdEncodingWithJwtForService() {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode();
        assertNotNull(encodedAccessToken);

        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    @Test
    public void verifyAccessTokenIdEncodingWithJwt() {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredServiceForJwtAccessTokenWithKeys(accessToken);
        val builder = getCipherEnabledJwtBuilder();
        val encoder = getAccessTokenEncoder(accessToken, builder, registeredService);

        val encodedAccessToken = encoder.encode();
        assertNotNull(encodedAccessToken);

        val decoded = encoder.decode(encodedAccessToken);
        assertNotNull(decoded);
        assertEquals(accessToken.getId(), decoded);
    }

    private OAuth20JwtBuilder getCipherEnabledJwtBuilder() {
        return new OAuth20JwtBuilder("http://cas.example.org/prefix",
            new OAuth20JwtAccessTokenCipherExecutor(true, true),
            servicesManager,
            new OAuth20RegisteredServiceJwtAccessTokenCipherExecutor());
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
