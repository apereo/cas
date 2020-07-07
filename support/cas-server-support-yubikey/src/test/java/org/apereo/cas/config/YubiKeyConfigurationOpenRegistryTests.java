package org.apereo.cas.config;

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
 * This is {@link YubiKeyConfigurationOpenRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
@TestPropertySource(properties = "cas.authn.mfa.yubikey.json-file=")
public class YubiKeyConfigurationOpenRegistryTests extends BaseYubiKeyTests {
    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Test
    public void verifyOperation() {
        val id = UUID.randomUUID().toString();
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(id));
    }

}
