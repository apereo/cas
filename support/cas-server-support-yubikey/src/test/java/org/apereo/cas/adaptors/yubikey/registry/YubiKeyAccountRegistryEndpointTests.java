package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAccountRegistryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.json-file=file:/tmp/yubikey.json",
        
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.yubikeyAccountRepository.enabled=true"
    })
@Tag("MFA")
public class YubiKeyAccountRegistryEndpointTests {

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubiKeyAccountRegistryEndpoint")
    private YubiKeyAccountRegistryEndpoint endpoint;

    @Test
    public void verifyOperation() {
        endpoint.deleteAll();
        val username = UUID.randomUUID().toString();
        assertTrue(endpoint.load().isEmpty());
        assertNull(endpoint.get(username));

        val request = YubiKeyDeviceRegistrationRequest.builder().username(username)
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(request));
        assertNotNull(endpoint.get(username));
        assertFalse(endpoint.load().isEmpty());
        endpoint.delete(username);
        assertNull(endpoint.get(username));
        endpoint.deleteAll();
        assertTrue(endpoint.load().isEmpty());
    }
}
