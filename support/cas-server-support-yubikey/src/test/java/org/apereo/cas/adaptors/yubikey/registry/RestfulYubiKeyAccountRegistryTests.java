package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.AbstractYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApiAuthentication")
@ExtendWith(CasTestExtension.class)
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.rest.url=http://localhost:${random.int[3000,9000]}"
    })
class RestfulYubiKeyAccountRegistryTests {
    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOps() {
        val port = URI.create(casProperties.getAuthn().getMfa().getYubikey().getRest().getUrl()).getPort();
        try (val webServer = new MockWebServer(port)) {
            webServer.responseBodySupplier(() -> new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8)));
            webServer.start();
            val request = YubiKeyDeviceRegistrationRequest.builder().username("casuser")
                .token(AbstractYubiKeyAccountRegistryTests.OTP).name(UUID.randomUUID().toString()).build();
            assertTrue(getYubiKeyAccountRegistry().registerAccountFor(request));
            assertDoesNotThrow(() -> {
                getYubiKeyAccountRegistry().delete(request.getUsername());
                getYubiKeyAccountRegistry().deleteAll();
            });

            var pubKey = getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(AbstractYubiKeyAccountRegistryTests.OTP);
            var registeredDevice = YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name("first-device")
                .publicId(getYubikeyAccountCipherExecutor().encode(pubKey).toString())
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .build();
            var account = YubiKeyAccount.builder()
                .devices(CollectionUtils.wrapList(registeredDevice))
                .username("casuser")
                .build();
            webServer.responseBodyJson(account);
            val request2 = YubiKeyDeviceRegistrationRequest.builder().username(account.getUsername())
                .token(AbstractYubiKeyAccountRegistryTests.OTP).name(UUID.randomUUID().toString()).build();
            assertTrue(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor(request2.getUsername()));
            assertDoesNotThrow(() -> getYubiKeyAccountRegistry().delete(account.getUsername(), registeredDevice.getId()));

            webServer.responseBodyJson(CollectionUtils.wrapList(account));
            assertFalse(getYubiKeyAccountRegistry().getAccounts().isEmpty());

            webServer.responseBodySupplier(() -> new ByteArrayResource("...".getBytes(StandardCharsets.UTF_8)));
            assertTrue(getYubiKeyAccountRegistry().getAccounts().isEmpty());
        }
    }
}
