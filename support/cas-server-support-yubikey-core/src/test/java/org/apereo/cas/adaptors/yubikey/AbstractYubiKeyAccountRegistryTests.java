package org.apereo.cas.adaptors.yubikey;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

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
    private static final String CASUSER = "casuser";

    public abstract YubiKeyAccountRegistry getYubiKeyAccountRegistry();

    @Test
    public void verifyAccountNotRegistered() {
        assertFalse(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor("missing-user"));
    }

    @Test
    public void verifyAccountNotRegisteredWithBadToken() {
        val yubiKeyAccountRegistry = getYubiKeyAccountRegistry();
        assertFalse(yubiKeyAccountRegistry.registerAccountFor(CASUSER, BAD_TOKEN));
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(CASUSER));
    }

    @Test
    public void verifyAccountRegistered() {
        val yubiKeyAccountRegistry = getYubiKeyAccountRegistry();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(CASUSER, OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(CASUSER));
        assertEquals(1, yubiKeyAccountRegistry.getAccounts().size());
    }

    @Test
    public void verifyEncryptedAccount() {
        val yubiKeyAccountRegistry = getYubiKeyAccountRegistry();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("encrypteduser", OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("encrypteduser", yubiKeyAccountRegistry.getAccountValidator().getTokenPublicId(OTP)));
    }


    @TestConfiguration("YubiKeyAccountRegistryTestConfiguration")
    @Lazy(false)
    public static class YubiKeyAccountRegistryTestConfiguration {
        @Bean
        @RefreshScope
        public YubiKeyAccountValidator yubiKeyAccountValidator() {
            return (uid, token) -> !token.equals(BAD_TOKEN);
        }
    }
}
