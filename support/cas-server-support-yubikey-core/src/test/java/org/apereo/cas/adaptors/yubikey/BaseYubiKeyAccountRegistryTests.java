package org.apereo.cas.adaptors.yubikey;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link BaseYubiKeyAccountRegistryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class BaseYubiKeyAccountRegistryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";
    private static final String BAD_TOKEN = "123456";
    private static final String CASUSER = "casuser";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Test
    public void verifyAccountNotRegistered() {
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("missing-user"));
    }

    @Test
    public void verifyAccountNotRegisteredWithBadToken() {
        assertFalse(yubiKeyAccountRegistry.registerAccountFor(CASUSER, BAD_TOKEN));
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(CASUSER));
    }

    @Test
    public void verifyAccountRegistered() {
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(CASUSER, OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor(CASUSER));
        assertEquals(1, yubiKeyAccountRegistry.getAccounts().size());
    }

    @Test
    public void verifyEncryptedAccount() {
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("encrypteduser", OTP));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("encrypteduser", yubiKeyAccountRegistry.getAccountValidator().getTokenPublicId(OTP)));
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
