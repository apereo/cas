package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAccountRegistryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFA")
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.yubikeyAccountRepository.enabled=true"
})
public class YubiKeyAccountRegistryEndpointTests extends BaseYubiKeyTests {

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
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(username, UUID.randomUUID().toString()));
        assertNotNull(endpoint.get(username));
        assertFalse(endpoint.load().isEmpty());
        endpoint.delete(username);
        assertNull(endpoint.get(username));
        endpoint.deleteAll();
        assertTrue(endpoint.load().isEmpty());
    }
}
