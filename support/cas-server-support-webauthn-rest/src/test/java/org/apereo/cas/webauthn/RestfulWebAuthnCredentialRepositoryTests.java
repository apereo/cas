package org.apereo.cas.webauthn;

import org.apereo.cas.config.RestfulWebAuthnConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("RestfulApiAuthentication")
@TestPropertySource(properties = "cas.authn.mfa.web-authn.rest.url=http://localhost:9559")
@Import(RestfulWebAuthnConfiguration.class)
class RestfulWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {

    @Test
    @Override
    protected void verifyOperation() throws Throwable {
        assertRegistrationIsFound();
        assertRegistrationBadStatus();
        assertRegistrationBadInput();
    }

    @Test
    void verifyLoadOperation() throws Throwable {
        assertLoadIsFound();
        assertLoadBadStatus();
        assertLoadBadInput();
    }

    @Test
    void verifyUpdate() throws Throwable {
        try (val webServer = new MockWebServer(9559, HttpStatus.OK)) {
            webServer.start();
            webAuthnCredentialRepository.removeAllRegistrations("casuser");
        }
    }

    private void assertRegistrationBadInput() {
        try (val webServer = new MockWebServer(9559,
            new ByteArrayResource("_-@@-_".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUsername("casuser");
            assertTrue(registration.isEmpty());
        }
    }

    private void assertLoadBadInput() throws Exception {
        val records = getCredentialRegistration("casuser");
        try (val webServer = new MockWebServer(9559,
            new ByteArrayResource("_-@@-_".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUserHandle(records.getUserIdentity().getId());
            assertTrue(registration.isEmpty());
        }
    }

    private void assertRegistrationBadStatus() {
        try (val webServer = new MockWebServer(9559,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.NOT_FOUND)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUsername("casuser");
            assertTrue(registration.isEmpty());
        }
    }

    private void assertLoadBadStatus() throws Exception {
        val records = getCredentialRegistration("casuser");
        try (val webServer = new MockWebServer(9559,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.NOT_FOUND)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUserHandle(records.getUserIdentity().getId());
            assertTrue(registration.isEmpty());
        }
    }

    private void assertRegistrationIsFound() throws Exception {
        val records = getCredentialRegistration("casuser");
        val results = cipherExecutor.encode(WebAuthnUtils.getObjectMapper()
            .writeValueAsString(CollectionUtils.wrapList(records)));
        try (val webServer = new MockWebServer(9559,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUsername("casuser");
            assertFalse(registration.isEmpty());
        }
    }

    private void assertLoadIsFound() throws Exception {
        val records = getCredentialRegistration("casuser");
        val results = cipherExecutor.encode(WebAuthnUtils.getObjectMapper()
            .writeValueAsString(CollectionUtils.wrapList(records)));
        try (val webServer = new MockWebServer(9559,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUserHandle(records.getUserIdentity().getId());
            assertFalse(registration.isEmpty());
        }
    }
}
