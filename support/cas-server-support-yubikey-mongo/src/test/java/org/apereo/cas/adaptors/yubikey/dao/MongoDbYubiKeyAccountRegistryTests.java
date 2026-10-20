package org.apereo.cas.adaptors.yubikey.dao;

import module java.base;
import org.apereo.cas.adaptors.yubikey.AbstractYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.config.CasMongoDbYubiKeyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
 * This is {@link MongoDbYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MongoDbMFA")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasMongoDbYubiKeyAutoConfiguration.class,
    BaseYubiKeyTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.yubikey.mongo.database-name=mfa-trusted",
        "cas.authn.mfa.yubikey.mongo.host=localhost",
        "cas.authn.mfa.yubikey.mongo.port=27017",
        "cas.authn.mfa.yubikey.mongo.drop-collection=true",
        "cas.authn.mfa.yubikey.mongo.user-id=root",
        "cas.authn.mfa.yubikey.mongo.password=secret",
        "cas.authn.mfa.yubikey.mongo.authentication-database-name=admin",
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As="
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 27017)
@Getter
class MongoDbYubiKeyAccountRegistryTests extends AbstractYubiKeyAccountRegistryTests {
    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Test
    void verifySavingTwiceKeepsOneAccount() {
        val username = UUID.randomUUID().toString();
        val first = YubiKeyRegisteredDevice.builder().id(System.currentTimeMillis()).name("first")
            .publicId(yubikeyAccountCipherExecutor.encode(UUID.randomUUID().toString()).toString())
            .registrationDate(ZonedDateTime.now(Clock.systemUTC())).build();
        yubiKeyAccountRegistry.save(YubiKeyAccount.builder().username(username).devices(new ArrayList<>(List.of(first))).build());
        val second = YubiKeyRegisteredDevice.builder().id(System.currentTimeMillis() + 1).name("second")
            .publicId(yubikeyAccountCipherExecutor.encode(UUID.randomUUID().toString()).toString())
            .registrationDate(ZonedDateTime.now(Clock.systemUTC())).build();
        yubiKeyAccountRegistry.save(YubiKeyAccount.builder().username(username).devices(new ArrayList<>(List.of(first, second))).build());
        val accounts = yubiKeyAccountRegistry.getAccounts().stream().filter(entry -> username.equals(entry.getUsername())).toList();
        assertEquals(1, accounts.size());
        assertEquals(2, yubiKeyAccountRegistry.getAccount(username).orElseThrow().getDevices().size());
        yubiKeyAccountRegistry.delete(username);
    }

    @Test
    void verifyDeviceRemovalForUnknownAccount() {
        assertDoesNotThrow(() -> yubiKeyAccountRegistry.delete(UUID.randomUUID().toString(), 1L));
    }
}
