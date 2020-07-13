package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.config.RedisYubiKeyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Redis")
@SpringBootTest(classes = {
    RedisYubiKeyConfiguration.class,
    BaseYubiKeyTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.yubikey.redis.host=localhost",
        "cas.authn.mfa.yubikey.redis.port=6379",
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As="
    })
@EnabledIfPortOpen(port = 6379)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisYubiKeyAccountRegistryTests {
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    private static final String BAD_TOKEN = "123456";

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Test
    public void verifyAccountNotRegistered() {
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("missing-user"));
    }

    @Test
    public void verifyAccountNotRegisteredWithBadToken() {
        val id = UUID.randomUUID().toString();
        assertFalse(yubiKeyAccountRegistry.registerAccountFor(id, BAD_TOKEN));
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(id));
    }

    @AfterEach
    public void afterEach() {
        yubiKeyAccountRegistry.deleteAll();
    }

    @Test
    public void verifyAccountRegistered() {
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser2", OTP));

        val id = UUID.randomUUID().toString();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(id, OTP));
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(id, OTP + OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(id));
        val account = yubiKeyAccountRegistry.getAccount(id);
        account.ifPresent(acct -> assertEquals(2, acct.getDeviceIdentifiers().size()));
    }

    @Test
    public void verifyEncryptedAccount() {
        val id = UUID.randomUUID().toString();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(id, OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(id,
            yubiKeyAccountRegistry.getAccountValidator().getTokenPublicId(OTP)));
    }

    @Test
    public void verifyAccounts() {
        val id = UUID.randomUUID().toString();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(id, OTP));
        assertFalse(yubiKeyAccountRegistry.getAccounts().isEmpty());
        yubiKeyAccountRegistry.delete(id);
        assertFalse(yubiKeyAccountRegistry.getAccount(id).isPresent());
        yubiKeyAccountRegistry.deleteAll();
        assertTrue(yubiKeyAccountRegistry.getAccounts().isEmpty());
    }
}
