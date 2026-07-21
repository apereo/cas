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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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

    private CredentialRegistration register(final Authentication authn) throws Exception {
        val registration = getCredentialRegistration(authn);
        val json = WebAuthnUtils.getObjectMapper().writeValueAsString(registration);
        assertNotNull(json);
        webAuthnCredentialRepository.addRegistrationByUsername(authn.getPrincipal().getId(), registration);
        return registration;
    }
}
