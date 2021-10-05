package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAccountRegistryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.json-file=file:${java.io.tmpdir}/yubikey.json",

        "management.endpoints.web.exposure.include=*",
        "management.endpoint.yubikeyAccountRepository.enabled=true"
    })
@Tag("MFAProvider")
public class YubiKeyAccountRegistryEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubiKeyAccountRegistryEndpoint")
    private YubiKeyAccountRegistryEndpoint endpoint;

    @Test
    public void verifyOperation() {
        endpoint.deleteAll();
        val username = UUID.randomUUID().toString();
        assertTrue(endpoint.load().isEmpty());
        assertNull(endpoint.get(username));

        val request = YubiKeyDeviceRegistrationRequest.builder().username(username)
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(request));
        assertNotNull(endpoint.get(username));
        assertFalse(endpoint.load().isEmpty());

        val entity = endpoint.export();
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        endpoint.delete(username);
        assertNull(endpoint.get(username));
        endpoint.deleteAll();
        assertTrue(endpoint.load().isEmpty());
    }

    @Test
    public void verifyImportOperation() throws Exception {
        val toSave = YubiKeyAccount.builder().username(UUID.randomUUID().toString())
            .devices(CollectionUtils.wrapList(YubiKeyRegisteredDevice.builder()
                .name(UUID.randomUUID().toString())
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .publicId(UUID.randomUUID().toString()).build()))
            .build();

        val request = new MockHttpServletRequest();
        val content = MAPPER.writeValueAsString(toSave);
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importAccount(request));
    }
}
