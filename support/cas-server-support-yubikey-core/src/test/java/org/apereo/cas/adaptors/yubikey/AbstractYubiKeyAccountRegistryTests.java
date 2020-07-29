package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractYubiKeyAccountRegistryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("MFA")
public abstract class AbstractYubiKeyAccountRegistryTests {
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    private static final String BAD_TOKEN = "123456";

    public abstract YubiKeyAccountRegistry getYubiKeyAccountRegistry();

    public abstract CipherExecutor getYubikeyAccountCipherExecutor();

    @BeforeEach
    public void setUp() {
        getYubiKeyAccountRegistry().deleteAll();
    }

    @Test
    public void verifyCipher() {
        val pubKey = getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(OTP);
        val encoded = getYubikeyAccountCipherExecutor().encode(pubKey);
        assertEquals(pubKey, getYubikeyAccountCipherExecutor().decode(encoded));
    }

    @Test
    public void verifyAccountNotRegistered() {
        assertFalse(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor("missing-user"));
    }

    @Test
    public void verifyAccountNotRegisteredWithBadToken() {
        val request = YubiKeyDeviceRegistrationRequest.builder().username("casuser")
            .token(BAD_TOKEN).name(UUID.randomUUID().toString()).build();
        assertFalse(getYubiKeyAccountRegistry().registerAccountFor(request));
        assertFalse(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor("casuser"));
    }

    @Test
    public void verifyAccountRegistered() {
        val request1 = YubiKeyDeviceRegistrationRequest.builder().username("casuser2")
            .token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(getYubiKeyAccountRegistry().registerAccountFor(request1));

        val request2 = YubiKeyDeviceRegistrationRequest.builder().username("casuser")
            .token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(getYubiKeyAccountRegistry().registerAccountFor(request2));

        val request3 = YubiKeyDeviceRegistrationRequest.builder().username("casuser")
            .token(OTP + OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(getYubiKeyAccountRegistry().registerAccountFor(request3));

        assertTrue(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor("casuser"));
        assertEquals(2, getYubiKeyAccountRegistry().getAccounts().size());
        val account = getYubiKeyAccountRegistry().getAccount("casuser");
        account.ifPresent(acct -> assertEquals(2, acct.getDevices().size()));

        getYubiKeyAccountRegistry().delete("casuser");
        assertTrue(getYubiKeyAccountRegistry().getAccount("casuser").isEmpty());
        getYubiKeyAccountRegistry().deleteAll();
    }

    @Test
    public void verifyEncryptedAccount() {
        val request1 = YubiKeyDeviceRegistrationRequest.builder().username("encrypteduser")
            .token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(getYubiKeyAccountRegistry().registerAccountFor(request1));

        assertTrue(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor("encrypteduser",
            getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(OTP)));
    }
}
