package org.apereo.cas.adaptors.yubikey;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractYubiKeyAccountRegistryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class AbstractYubiKeyAccountRegistryTests {
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";
    private static final String BAD_TOKEN = "123456";
    private static final String CASUSER = "casuser";

    public abstract YubiKeyAccountRegistry getYubiKeyAccountRegistry();

    @Test
    public void verifyAccountNotRegistered() {
        assertFalse(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor("missing-user"));
    }

    @Test
    public void verifyAccountNotRegisteredWithBadToken() {
        assertFalse(getYubiKeyAccountRegistry().registerAccountFor(CASUSER, BAD_TOKEN));
        assertFalse(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor(CASUSER));
    }

    @Test
    public void verifyAccountRegistered() {
        assertTrue(getYubiKeyAccountRegistry().registerAccountFor(CASUSER, OTP));
        assertTrue(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor(CASUSER));
        assertEquals(1, getYubiKeyAccountRegistry().getAccounts().size());
    }

    @Test
    public void verifyEncryptedAccount() {
        assertTrue(getYubiKeyAccountRegistry().registerAccountFor("encrypteduser", OTP));
        assertTrue(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor("encrypteduser", getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(OTP)));
    }


    @TestConfiguration("YubiKeyAccountRegistryTestConfiguration")
    public static class YubiKeyAccountRegistryTestConfiguration {
        @Bean
        @RefreshScope
        public YubiKeyAccountValidator yubiKeyAccountValidator() {
            return (uid, token) -> !token.equals(BAD_TOKEN);
        }
    }
}
