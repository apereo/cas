package org.apereo.cas.adaptors.yubikey;

import module java.base;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractYubiKeyAccountRegistryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class AbstractYubiKeyAccountRegistryTests {
    public static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    private static final String BAD_TOKEN = "123456";

    public abstract YubiKeyAccountRegistry getYubiKeyAccountRegistry();

    public abstract CipherExecutor getYubikeyAccountCipherExecutor();

    @BeforeEach
    void setUp() {
        getYubiKeyAccountRegistry().deleteAll();
    }

    @Test
    void verifyCipher() {
        val pubKey = getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(OTP);
        val encoded = getYubikeyAccountCipherExecutor().encode(pubKey);
        assertEquals(pubKey, getYubikeyAccountCipherExecutor().decode(encoded));
    }

    @Test
    void verifyAccountNotRegistered() {
        assertFalse(isYubiKeyRegisteredFor("missing-user", null));
    }

    @Test
    void verifyAccountNotRegisteredWithBadToken() {
        val casuser = UUID.randomUUID().toString();
        val request = YubiKeyDeviceRegistrationRequest.builder().username(casuser)
            .token(BAD_TOKEN).name(UUID.randomUUID().toString()).build();
        assertFalse(registerAccount(request));
        assertFalse(isYubiKeyRegisteredFor(casuser, null));
    }

    @Test
    void verifyAccountRegistered() {
        val casuser = UUID.randomUUID().toString();
        val request1 = YubiKeyDeviceRegistrationRequest.builder().username("casuser2")
            .token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(registerAccount(request1));

        val request2 = YubiKeyDeviceRegistrationRequest.builder().username(casuser)
            .token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(registerAccount(request2));

        val request3 = YubiKeyDeviceRegistrationRequest.builder().username(casuser)
            .token("%s%s".formatted(OTP, OTP)).name(UUID.randomUUID().toString()).build();
        assertTrue(registerAccount(request3));

        assertTrue(isYubiKeyRegisteredFor(casuser, null));
        val account = getAccount(casuser);
        account.ifPresent(acct -> assertEquals(2, acct.getDevices().size()));

        getYubiKeyAccountRegistry().delete(casuser);
        assertTrue(getAccount(casuser).isEmpty());
        getYubiKeyAccountRegistry().deleteAll();
        assertEquals(0, getAccounts().size());
    }

    @Test
    void verifySaveAccount() {
        val account = YubiKeyAccount.builder().username(UUID.randomUUID().toString())
            .devices(List.of(YubiKeyRegisteredDevice.builder()
                .name(UUID.randomUUID().toString())
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .publicId(UUID.randomUUID().toString()).build()))
            .build();
        assertNotNull(getYubiKeyAccountRegistry().save(account));
        getYubiKeyAccountRegistry().delete(account.getUsername());
        getYubiKeyAccountRegistry().deleteAll();
    }

    @Test
    void verifyDeviceRemoval() {
        val username = "casuser-registered-device";
        for (var i = 0; i < 4; i++) {
            val request = YubiKeyDeviceRegistrationRequest.builder()
                .username("casuser-registered-device")
                .token(OTP)
                .name(UUID.randomUUID().toString())
                .build();
            assertTrue(registerAccount(request));
            assertTrue(isYubiKeyRegisteredFor(request.getUsername(), null));
        }
        var account = getAccount(username);
        assertTrue(account.isPresent());
        assertFalse(account.get().getDevices().isEmpty());

        account.get().getDevices().forEach(device -> getYubiKeyAccountRegistry().delete(username, device.getId()));
        account = getAccount(username);
        assertTrue(account.get().getDevices().isEmpty());
    }

    @Test
    void verifyEncryptedAccount() {
        val request1 = YubiKeyDeviceRegistrationRequest.builder().username("encrypteduser")
            .token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(registerAccount(request1));

        assertTrue(isYubiKeyRegisteredFor("encrypteduser",
            getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(OTP)));
    }

    protected Collection<? extends YubiKeyAccount> getAccounts() {
        return getYubiKeyAccountRegistry().getAccounts();
    }

    protected Optional<? extends YubiKeyAccount> getAccount(final String username) {
        return getYubiKeyAccountRegistry().getAccount(username);
    }

    protected boolean isYubiKeyRegisteredFor(final String username, final String pubKey) {
        if (StringUtils.isNotBlank(pubKey)) {
            return getYubiKeyAccountRegistry().isYubiKeyRegisteredFor(username, pubKey);
        }
        return getYubiKeyAccountRegistry().isYubiKeyRegisteredFor(username);
    }

    protected boolean registerAccount(final YubiKeyDeviceRegistrationRequest request) {
        return getYubiKeyAccountRegistry().registerAccountFor(request);
    }
}
