package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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
        "cas.authn.mfa.yubikey.json.location=file:${java.io.tmpdir}/yubikey.json",
        "cas.authn.mfa.yubikey.json.watch-resource=false",

        "management.endpoints.web.exposure.include=*",
        "management.endpoint.yubikeyAccountRepository.access=UNRESTRICTED"
    })
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "yubiKeyAccountRegistry", mode = ResourceAccessMode.READ_WRITE)
class YubiKeyAccountRegistryEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubiKeyAccountRegistryEndpoint")
    private YubiKeyAccountRegistryEndpoint endpoint;

    @Test
    void verifyOperation() {
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
    void verifyImportOperation() throws Throwable {
        val toSave = YubiKeyDeviceRegistrationRequest.builder().username(UUID.randomUUID().toString())
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build();

        val request = new MockHttpServletRequest();
        val content = MAPPER.writeValueAsString(toSave);
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importAccount(request).getStatusCode());
    }
}
