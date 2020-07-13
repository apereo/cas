package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaYubiKeyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    JpaYubiKeyConfiguration.class,
    CasHibernateJpaConfiguration.class,
    BaseYubiKeyTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.mfa.yubikey.client-id=18423",
    "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
    "cas.jdbc.show-sql=true",
    "cas.authn.mfa.yubikey.jpa.ddl-auto=create-drop"
})
@Tag("JDBC")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaYubiKeyAccountRegistryTests {
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
        assertFalse(yubiKeyAccountRegistry.registerAccountFor("casuser", BAD_TOKEN));
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("casuser"));
    }

    @Test
    public void verifyAccountRegistered() {
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser", "cccccccvlidchlffblbghhckbctgethcrtdrruchvlud"));
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser", "cccccccvlidchlffblbghhckbctgethcrtdrruchvluq"));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("casuser"));
        assertEquals(1, yubiKeyAccountRegistry.getAccounts().size());
        val account = yubiKeyAccountRegistry.getAccount("casuser");
        account.ifPresent(acct -> assertEquals(2, acct.getDeviceIdentifiers().size()));

        yubiKeyAccountRegistry.delete("casuser");
        assertTrue(yubiKeyAccountRegistry.getAccount("casuser").isEmpty());
        yubiKeyAccountRegistry.deleteAll();
    }
}
