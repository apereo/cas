package org.apereo.cas.webauthn.web;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.webauthn.WebAuthnUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link WebAuthnRegisteredDevicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class, properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.webAuthnDevices.access=UNRESTRICTED"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("ActuatorEndpoint")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = WebAuthnCredentialRepository.BEAN_NAME, mode = ResourceAccessMode.READ_WRITE)
class WebAuthnRegisteredDevicesEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;
    
    private static CredentialRegistration getCredentialRegistration(final Authentication authn) throws Exception {
        return CredentialRegistration.builder()
            .userIdentity(UserIdentity.builder()
                .name(authn.getPrincipal().getId())
                .displayName("CAS")
                .id(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                .build())
            .registrationTime(Instant.now(Clock.systemUTC()))
            .credential(RegisteredCredential.builder()
                .credentialId(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .build())
            .build();
    }

    @Test
    void verifyOperation() throws Throwable {
        val id1 = UUID.randomUUID().toString();
        register(RegisteredServiceTestUtils.getAuthentication(id1));

        val id2 = UUID.randomUUID().toString();
        register(RegisteredServiceTestUtils.getAuthentication(id2));

        mockMvc.perform(get("/actuator/webAuthnDevices/%s".formatted(id1))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
        mockMvc.perform(get("/actuator/webAuthnDevices/%s".formatted(id2))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
        
        var principal = RegisteredServiceTestUtils.getPrincipal(id1);
        val devices = webAuthnMultifactorAuthenticationProvider.getDeviceManager().findRegisteredDevices(principal);
        assertEquals(1, devices.size());
        
        mockMvc.perform(delete("/actuator/webAuthnDevices/%s/%s".formatted(id1, id1))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/webAuthnDevices/%s".formatted(id1))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(delete("/actuator/webAuthnDevices/%s".formatted(id2))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/webAuthnDevices/%s".formatted(id1))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/actuator/webAuthnDevices/%s".formatted(id2))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        val id3 = UUID.randomUUID().toString();
        val record = getCredentialRegistration(RegisteredServiceTestUtils.getAuthentication(id3));
        mockMvc.perform(post("/actuator/webAuthnDevices/%s".formatted(id3))
                .param("record", EncodingUtils.encodeBase64(WebAuthnUtils.getObjectMapper().writeValueAsString(record)))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        val id4 = UUID.randomUUID().toString();
        principal = RegisteredServiceTestUtils.getPrincipal(id4);
        val registration = register(RegisteredServiceTestUtils.getAuthentication(id4));
        webAuthnMultifactorAuthenticationProvider.getDeviceManager().removeRegisteredDevice(principal,
            registration.getCredential().getCredentialId().getBase64Url());
        assertFalse(webAuthnMultifactorAuthenticationProvider.getDeviceManager().hasRegisteredDevices(principal));
    }

    @Test
    void verifyImportExport() throws Throwable {
        val id1 = UUID.randomUUID().toString();
        register(RegisteredServiceTestUtils.getAuthentication(id1));
        mockMvc.perform(get("/actuator/webAuthnDevices/export")
                .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment")));

        val toSave = getCredentialRegistration(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()));
        val content = WebAuthnUtils.getObjectMapper().writeValueAsString(toSave);
        mockMvc.perform(post("/actuator/webAuthnDevices/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isCreated());
    }

    @Test
    void verifyDeleteAllAndArchiveImport() throws Throwable {
        val username = UUID.randomUUID().toString();
        register(RegisteredServiceTestUtils.getAuthentication(username));
        mockMvc.perform(get("/actuator/webAuthnDevices")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].userIdentity.name", hasItem(username)));
        val archive = mockMvc.perform(get("/actuator/webAuthnDevices/export"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        mockMvc.perform(delete("/actuator/webAuthnDevices")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        try (val registrations = webAuthnCredentialRepository.stream()) {
            assertEquals(0, registrations.count());
        }
        mockMvc.perform(get("/actuator/webAuthnDevices").param("summary", "true")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").value(0))
            .andExpect(jsonPath("$.users").value(0));

        mockMvc.perform(post("/actuator/webAuthnDevices/import")
                .contentType("application/zip")
                .content(archive)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.imported").isNumber());
        mockMvc.perform(get("/actuator/webAuthnDevices/%s".formatted(username))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
    }

    private CredentialRegistration register(final Authentication authn) throws Exception {
        val registration = getCredentialRegistration(authn);
        val json = WebAuthnUtils.getObjectMapper().writeValueAsString(registration);
        assertNotNull(json);
        webAuthnCredentialRepository.addRegistrationByUsername(authn.getPrincipal().getId(), registration);
        return registration;
    }

    @Test
    void verifyArchiveImportCountsRefusedRecords() throws Throwable {
        val content = WebAuthnUtils.getObjectMapper().writeValueAsString(
            getCredentialRegistration(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString())));
        mockMvc.perform(post("/actuator/webAuthnDevices/import")
                .contentType("application/zip")
                .content(archiveOf(content, "not a record"))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.imported").value(1))
            .andExpect(jsonPath("$.skipped").value(1));
        mockMvc.perform(post("/actuator/webAuthnDevices/import")
                .contentType("application/zip")
                .content(archiveOf(content, "not a record"))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.imported").value(1))
            .andExpect(jsonPath("$.skipped").value(1));
        mockMvc.perform(post("/actuator/webAuthnDevices/import")
                .contentType("application/zip")
                .content(archiveOf("not a record", "[1, 2]"))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.imported").value(0))
            .andExpect(jsonPath("$.skipped").value(2));
        mockMvc.perform(post("/actuator/webAuthnDevices/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not a record"))
            .andExpect(status().isBadRequest());
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
