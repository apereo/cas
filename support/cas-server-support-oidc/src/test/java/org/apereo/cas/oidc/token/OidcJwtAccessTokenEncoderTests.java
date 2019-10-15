package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.AccessToken;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJwtAccessTokenEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oauth.accessToken.crypto.encryption-enabled=false")
public class OidcJwtAccessTokenEncoderTests extends AbstractOidcTests {
    private String encodeAccessToken(final AccessToken accessToken, final OidcRegisteredService registeredService) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(accessToken.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .encode();
    }

    @Test
    public void verifyEncodingWithoutEncryptionForService() {
        val accessToken = getAccessToken();
        val registeredService = getOidcRegisteredService(accessToken.getClientId());
        registeredService.setJwtAccessToken(true);

        val property = new DefaultRegisteredServiceProperty("false");
        registeredService.setProperties(Map.of(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            property
        ));
        this.servicesManager.save(registeredService);

        val token1 = encodeAccessToken(accessToken, registeredService);
        val token2 = encodeAccessToken(accessToken, registeredService);
        assertEquals(token1, token2);
    }

    @Test
    public void verifyEncodingWithNoCiphersForService() {
        val accessToken = getAccessToken(StringUtils.EMPTY, "encoding-service-clientid");
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

        val token1 = encodeAccessToken(accessToken, registeredService);
        val token2 = encodeAccessToken(accessToken, registeredService);
        assertEquals(token1, token2);
    }
}
