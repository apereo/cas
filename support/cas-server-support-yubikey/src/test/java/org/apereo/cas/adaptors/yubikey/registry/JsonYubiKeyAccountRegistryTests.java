package org.apereo.cas.adaptors.yubikey.registry;

import module java.base;
import org.apereo.cas.adaptors.yubikey.AbstractYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("FileSystem")
@ExtendWith(CasTestExtension.class)
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.json.location=file:${java.io.tmpdir}/cas-yubikey-${random.uuid}.json",
        "cas.authn.mfa.yubikey.json.watch-resource=false"
    })
class JsonYubiKeyAccountRegistryTests extends AbstractYubiKeyAccountRegistryTests {
    private static final int ACCOUNT_COUNT = 200;

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;

    @RepeatedTest(5)
    void verifyConcurrentChangesKeepFileReadable(@TempDir final Path directory) throws Exception {
        val file = directory.resolve("yubikey.json");
        val registry = new JsonYubiKeyAccountRegistry(new FileSystemResource(file), false, (uid, token) -> true);
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val results = executor.invokeAll(IntStream.range(0, ACCOUNT_COUNT)
                .<Callable<Boolean>>mapToObj(index -> () -> {
                    val username = "user-" + index;
                    registry.save(YubiKeyAccount.builder()
                        .username(username)
                        .devices(CollectionUtils.wrapList(newDevice(1), newDevice(2), newDevice(3)))
                        .build());
                    registry.delete(username, 2);
                    return Boolean.TRUE;
                })
                .toList());
            for (val result : results) {
                assertTrue(result.get());
            }
        } finally {
            registry.destroy();
        }
        val reloaded = new JsonYubiKeyAccountRegistry(new FileSystemResource(file), false, (uid, token) -> true);
        val accounts = reloaded.getAccountsInternal();
        assertEquals(ACCOUNT_COUNT, accounts.size());
        assertTrue(accounts.stream().allMatch(account -> account.getDevices().size() == 2));
        try (val files = Files.list(directory)) {
            assertEquals(List.of(file), files.toList());
        }
    }

    private static YubiKeyRegisteredDevice newDevice(final long id) {
        return YubiKeyRegisteredDevice.builder()
            .id(id)
            .name("device-" + id)
            .publicId("public-id-" + id)
            .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
            .build();
    }
}
