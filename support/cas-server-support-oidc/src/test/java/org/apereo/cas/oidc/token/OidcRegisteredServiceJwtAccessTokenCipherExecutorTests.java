package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceJwtAccessTokenCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcRegisteredServiceJwtAccessTokenCipherExecutorTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val service = getOidcRegisteredService("whatever");
        assertTrue(oauthRegisteredServiceJwtAccessTokenCipherExecutor.supports(service));
        val at = getAccessToken();
        val encoded = oauthRegisteredServiceJwtAccessTokenCipherExecutor.encode(at.getId(), Optional.of(service));
        assertNotNull(encoded);
        val decoded = oauthRegisteredServiceJwtAccessTokenCipherExecutor.decode(encoded, Optional.of(service));
        assertNotNull(decoded);
        assertEquals(at.getId(), decoded);
    }

    @Test
    public void verifyNoSigningKey() {
        val service = getOidcRegisteredService("whatever");
        service.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty(RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED.getDefaultValue()));

        val key = EncodingUtils.generateJsonWebKey(512);
        service.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_KEY.getPropertyName(),
            new DefaultRegisteredServiceProperty(key));
        val at = getAccessToken();
        val encoded = oauthRegisteredServiceJwtAccessTokenCipherExecutor.encode(at.getId(), Optional.of(service));
        assertNotNull(encoded);
    }

    @Test
    public void verifyEncKey() {
        val service = getOidcRegisteredService("whatever");
        service.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));

        val key = EncodingUtils.newJsonWebKey(2048);
        service.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY.getPropertyName(),
            new DefaultRegisteredServiceProperty(key.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE)));
        val at = getAccessToken();
        val encoded = oauthRegisteredServiceJwtAccessTokenCipherExecutor.encode(at.getId(), Optional.of(service));
        assertNotNull(encoded);
    }

    @Test
    public void verifyNoEncKey() {
        val service = getOidcRegisteredService("whatever");
        service.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        val at = getAccessToken();
        val encoded = oauthRegisteredServiceJwtAccessTokenCipherExecutor.encode(at.getId(), Optional.of(service));
        assertNotNull(encoded);
    }
}
