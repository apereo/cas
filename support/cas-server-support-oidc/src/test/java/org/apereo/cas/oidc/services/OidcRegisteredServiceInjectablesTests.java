package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceInjectablesTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDCServices")
@TestPropertySource(properties = {
    "cas.authn.oidc.services.defaults.jwksKeyId=12345",
    "cas.authn.oidc.services.defaults.idTokenSigningAlg=SHA512"
})
class OidcRegisteredServiceInjectablesTests extends AbstractOidcTests {

    @Test
    void verifyOperation() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        assertNull(registeredService.getJwksKeyId());
        assertNull(registeredService.getIdTokenSigningAlg());
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val serialized = serializer.toString(registeredService);
        val processedService = (OidcRegisteredService) serializer.from(serialized);
        assertEquals("12345", processedService.getJwksKeyId());
        assertEquals("SHA512", processedService.getIdTokenSigningAlg());
    }
}
