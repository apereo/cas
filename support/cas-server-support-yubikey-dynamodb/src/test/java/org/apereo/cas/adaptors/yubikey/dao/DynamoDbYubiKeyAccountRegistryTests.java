package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.config.DynamoDbYubiKeyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("DynamoDb")
@SpringBootTest(classes = {
    DynamoDbYubiKeyConfiguration.class,
    BaseYubiKeyTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.yubikey.dynamo-db.endpoint=http://localhost:8000",
        "cas.authn.mfa.yubikey.dynamo-db.drop-tables-on-startup=true",
        "cas.authn.mfa.yubikey.dynamo-db.local-instance=true",
        "cas.authn.mfa.yubikey.dynamo-db.region=us-east-1",
        "cas.authn.mfa.yubikey.dynamo-db.asynchronous=false",
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As="
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 8000)
public class DynamoDbYubiKeyAccountRegistryTests {
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    private static final String BAD_TOKEN = "123456";

    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;

    @BeforeEach
    public void beforeEach() {
        yubiKeyAccountRegistry.deleteAll();
    }

    @Test
    public void verifyCipher() {
        val pubKey = yubiKeyAccountRegistry.getAccountValidator().getTokenPublicId(OTP);
        val encoded = yubikeyAccountCipherExecutor.encode(pubKey);
        assertEquals(pubKey, yubikeyAccountCipherExecutor.decode(encoded));
    }

    @Test
    public void verifyAccountNotRegistered() {
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("missing-user"));
    }

    @Test
    public void verifyAccountNotRegisteredWithBadToken() {
        assertFalse(yubiKeyAccountRegistry.registerAccountFor("casuser", BAD_TOKEN));
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("casuser"));
    }

    @Test
    public void verifyAccountRegistered() {
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser2", OTP));
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser", OTP));
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser", OTP + OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("casuser"));
        assertEquals(2, yubiKeyAccountRegistry.getAccounts().size());
        val account = yubiKeyAccountRegistry.getAccount("casuser");
        account.ifPresent(acct -> assertEquals(2, acct.getDeviceIdentifiers().size()));

        yubiKeyAccountRegistry.delete("casuser");
        assertTrue(yubiKeyAccountRegistry.getAccount("casuser").isEmpty());
        yubiKeyAccountRegistry.deleteAll();
    }

    @Test
    public void verifyEncryptedAccount() {
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("encrypteduser", OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("encrypteduser",
            yubiKeyAccountRegistry.getAccountValidator().getTokenPublicId(OTP)));
    }
}
