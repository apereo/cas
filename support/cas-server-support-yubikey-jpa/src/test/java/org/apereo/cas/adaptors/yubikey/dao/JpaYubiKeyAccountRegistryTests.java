package org.apereo.cas.adaptors.yubikey.dao;

import module java.base;
import org.apereo.cas.adaptors.yubikey.AbstractYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJpaYubiKeyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    CasJpaYubiKeyAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseYubiKeyTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.mfa.yubikey.client-id=18423",
    "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
    "cas.jdbc.show-sql=false",
    "cas.authn.mfa.yubikey.jpa.ddl-auto=create-drop",
    "cas.authn.mfa.yubikey.jpa.url=jdbc:hsqldb:mem:yubikey;hsqldb.tx=mvcc"
})
@Tag("JDBCMFA")
@ExtendWith(CasTestExtension.class)
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
class JpaYubiKeyAccountRegistryTests extends AbstractYubiKeyAccountRegistryTests {
    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Test
    void verifyDeleteUnknownAccount() {
        assertDoesNotThrow(() -> yubiKeyAccountRegistry.delete(UUID.randomUUID().toString()));
    }

    @Test
    void verifySavingTwiceKeepsOneAccount() {
        val username = UUID.randomUUID().toString();
        val device = YubiKeyRegisteredDevice.builder().id(System.currentTimeMillis()).name("first")
            .publicId(yubikeyAccountCipherExecutor.encode(UUID.randomUUID().toString()).toString()).registrationDate(ZonedDateTime.now(Clock.systemUTC())).build();
        val account = YubiKeyAccount.builder().username(username).devices(new ArrayList<>(List.of(device))).build();
        yubiKeyAccountRegistry.save(account);
        val second = YubiKeyRegisteredDevice.builder().id(System.currentTimeMillis() + 1).name("second")
            .publicId(yubikeyAccountCipherExecutor.encode(UUID.randomUUID().toString()).toString()).registrationDate(ZonedDateTime.now(Clock.systemUTC())).build();
        yubiKeyAccountRegistry.save(YubiKeyAccount.builder().username(username).devices(new ArrayList<>(List.of(device, second))).build());
        val accounts = yubiKeyAccountRegistry.getAccounts().stream().filter(entry -> username.equals(entry.getUsername())).toList();
        assertEquals(1, accounts.size());
        assertEquals(2, yubiKeyAccountRegistry.getAccount(username).orElseThrow().getDevices().size());
        yubiKeyAccountRegistry.delete(username);
        assertTrue(yubiKeyAccountRegistry.getAccount(username).isEmpty());
    }

}
