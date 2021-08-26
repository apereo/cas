package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.AbstractYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

/**
 * This is {@link RestfulYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApiAuthentication")
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.rest.url=http://localhost:6591"
    })
public class RestfulYubiKeyAccountRegistryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;

    @Test
    public void verifyRegistration() {
        try (val webServer = new MockWebServer(6591,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), OK)) {
            webServer.start();
            val request = YubiKeyDeviceRegistrationRequest.builder().username("casuser")
                .token(AbstractYubiKeyAccountRegistryTests.OTP).name(UUID.randomUUID().toString()).build();
            assertTrue(getYubiKeyAccountRegistry().registerAccountFor(request));
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() throws Throwable {
                    getYubiKeyAccountRegistry().delete(request.getUsername());
                    getYubiKeyAccountRegistry().deleteAll();
                }
            });
        }
    }

    @Test
    public void verifyAccount() throws Exception {
        val pubKey = getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(AbstractYubiKeyAccountRegistryTests.OTP);
        val registeredDevice = YubiKeyRegisteredDevice.builder()
            .id(System.currentTimeMillis())
            .name("first-device")
            .publicId(getYubikeyAccountCipherExecutor().encode(pubKey).toString())
            .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
            .build();
        val account = YubiKeyAccount.builder()
            .devices(CollectionUtils.wrapList(registeredDevice))
            .username("casuser")
            .build();
        try (val webServer = new MockWebServer(6591,
            new ByteArrayResource(MAPPER.writeValueAsString(account).getBytes(StandardCharsets.UTF_8), "Output"), OK)) {
            webServer.start();

            val request = YubiKeyDeviceRegistrationRequest.builder().username(account.getUsername())
                .token(AbstractYubiKeyAccountRegistryTests.OTP).name(UUID.randomUUID().toString()).build();
            assertTrue(getYubiKeyAccountRegistry().isYubiKeyRegisteredFor(request.getUsername()));

            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() throws Throwable {
                    getYubiKeyAccountRegistry().delete(account.getUsername(), registeredDevice.getId());
                }
            });
        }
    }

    @Test
    public void verifyAccounts() throws Exception {
        val pubKey = getYubiKeyAccountRegistry().getAccountValidator().getTokenPublicId(AbstractYubiKeyAccountRegistryTests.OTP);
        val registeredDevice = YubiKeyRegisteredDevice.builder()
            .id(System.currentTimeMillis())
            .name("first-device")
            .publicId(getYubikeyAccountCipherExecutor().encode(pubKey).toString())
            .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
            .build();
        val account = YubiKeyAccount.builder()
            .devices(CollectionUtils.wrapList(registeredDevice))
            .username("casuser")
            .build();
        try (val webServer = new MockWebServer(6591,
            new ByteArrayResource(MAPPER.writeValueAsString(CollectionUtils.wrapList(account))
                .getBytes(StandardCharsets.UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(getYubiKeyAccountRegistry().getAccounts().isEmpty());
        }
    }

    @Test
    public void verifyFailsAccount() {
        try (val webServer = new MockWebServer(6591,
            new ByteArrayResource("...".getBytes(StandardCharsets.UTF_8), "Output"), OK)) {
            webServer.start();
            assertTrue(getYubiKeyAccountRegistry().getAccounts().isEmpty());
        }
    }
}
