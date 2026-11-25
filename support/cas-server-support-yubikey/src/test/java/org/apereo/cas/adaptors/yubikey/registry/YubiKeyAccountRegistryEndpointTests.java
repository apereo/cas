package org.apereo.cas.adaptors.yubikey.registry;

import module java.base;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link YubiKeyAccountRegistryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class, properties = {
    "cas.authn.mfa.yubikey.client-id=18423",
    "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
    "cas.authn.mfa.yubikey.json.location=file:${java.io.tmpdir}/cas-yubikey-${random.uuid}.json",
    "cas.authn.mfa.yubikey.json.watch-resource=false",

    "management.endpoints.web.exposure.include=*",
    "management.endpoint.yubikeyAccountRepository.access=UNRESTRICTED"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "yubiKeyAccountRegistry", mode = ResourceAccessMode.READ_WRITE)
class YubiKeyAccountRegistryEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(delete("/actuator/yubikeyAccountRepository")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        val username = UUID.randomUUID().toString();
        mockMvc.perform(get("/actuator/yubikeyAccountRepository")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/actuator/yubikeyAccountRepository/%s".formatted(username))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(StringUtils.EMPTY));

        val request = YubiKeyDeviceRegistrationRequest.builder().username(username)
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(request));
        mockMvc.perform(get("/actuator/yubikeyAccountRepository/%s".formatted(username))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
        mockMvc.perform(get("/actuator/yubikeyAccountRepository")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());

        mockMvc.perform(get("/actuator/yubikeyAccountRepository/export")
                .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment")));

        mockMvc.perform(delete("/actuator/yubikeyAccountRepository/%s".formatted(username))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        assertTrue(yubiKeyAccountRegistry.getAccount(username).isEmpty());
        mockMvc.perform(delete("/actuator/yubikeyAccountRepository")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        assertTrue(yubiKeyAccountRegistry.getAccounts().isEmpty());
    }

    @Test
    void verifyExportedArchiveImportsBack() throws Throwable {
        val username = UUID.randomUUID().toString();
        val request = YubiKeyDeviceRegistrationRequest.builder().username(username)
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(request));
        val registered = yubiKeyAccountRegistry.getAccount(username).orElseThrow().getDevices().getFirst().getPublicId();
        mockMvc.perform(get("/actuator/yubikeyAccountRepository").param("summary", "true")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").isNumber())
            .andExpect(jsonPath("$.devices").isNumber());
        val archive = mockMvc.perform(get("/actuator/yubikeyAccountRepository/export"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
        yubiKeyAccountRegistry.delete(username);
        assertTrue(yubiKeyAccountRegistry.getAccount(username).isEmpty());

        mockMvc.perform(post("/actuator/yubikeyAccountRepository/import")
                .contentType("application/zip")
                .content(archive)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.imported").isNumber());
        val imported = yubiKeyAccountRegistry.getAccount(username).orElseThrow();
        assertEquals(registered, imported.getDevices().getFirst().getPublicId());
    }

    @Test
    void verifyExportedAccountImports() throws Throwable {
        val username = UUID.randomUUID().toString();
        val request = YubiKeyDeviceRegistrationRequest.builder().username(username)
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build();
        assertTrue(yubiKeyAccountRegistry.registerAccountFor(request));
        val account = yubiKeyAccountRegistry.getAccount(username).orElseThrow();
        val content = MAPPER.writeValueAsString(account);
        yubiKeyAccountRegistry.delete(username);

        mockMvc.perform(post("/actuator/yubikeyAccountRepository/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isCreated());
        val imported = yubiKeyAccountRegistry.getAccount(username).orElseThrow();
        assertEquals(account.getDevices().getFirst().getPublicId(), imported.getDevices().getFirst().getPublicId());
    }

    @Test
    void verifyImportOperation() throws Throwable {
        val toSave = YubiKeyDeviceRegistrationRequest.builder().username(UUID.randomUUID().toString())
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build();

        val content = MAPPER.writeValueAsString(toSave);
        mockMvc.perform(post("/actuator/yubikeyAccountRepository/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isCreated());
    }

    @Test
    void verifyArchiveImportCountsRefusedRecords() throws Throwable {
        val content = MAPPER.writeValueAsString(YubiKeyDeviceRegistrationRequest.builder().username(UUID.randomUUID().toString())
            .token(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).build());
        mockMvc.perform(post("/actuator/yubikeyAccountRepository/import")
                .contentType("application/zip")
                .content(archiveOf(content, "not a record"))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.imported").value(1))
            .andExpect(jsonPath("$.skipped").value(1));
        mockMvc.perform(post("/actuator/yubikeyAccountRepository/import")
                .contentType("application/zip")
                .content(archiveOf("not a record", "[1, 2]"))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.imported").value(0))
            .andExpect(jsonPath("$.skipped").value(2));
        mockMvc.perform(post("/actuator/yubikeyAccountRepository/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not a record"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/actuator/yubikeyAccountRepository/import")
                .contentType("application/zip")
                .content(archiveOf(content, "x".repeat(1024 * 1024 + 1)))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.CONTENT_TOO_LARGE.value()));
    }

    private static byte[] archiveOf(final String... records) throws Exception {
        val bytes = new ByteArrayOutputStream();
        try (val zip = new ZipOutputStream(bytes)) {
            for (var index = 0; index < records.length; index++) {
                zip.putNextEntry(new ZipEntry("record-%s.json".formatted(index)));
                zip.write(records[index].getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
