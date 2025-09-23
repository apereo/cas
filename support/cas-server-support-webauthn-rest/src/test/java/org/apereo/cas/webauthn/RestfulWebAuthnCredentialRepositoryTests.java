package org.apereo.cas.webauthn;

import org.apereo.cas.config.CasRestfulWebAuthnAutoConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("RestfulApiAuthentication")
@TestPropertySource(properties = "cas.authn.mfa.web-authn.rest.url=http://localhost:${random.int[3000,9000]}")
@ImportAutoConfiguration(CasRestfulWebAuthnAutoConfiguration.class)
@Execution(ExecutionMode.SAME_THREAD)
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
    void verifyUpdate() {
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getRest();
        val port = URI.create(props.getUrl()).getPort();
        try (val webServer = new MockWebServer(port, HttpStatus.OK)) {
            webServer.start();
            webAuthnCredentialRepository.removeAllRegistrations("casuser");
        }
    }

    private void assertRegistrationBadInput() {
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getRest();
        val port = URI.create(props.getUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource("_-@@-_".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUsername("casuser");
            assertTrue(registration.isEmpty());
        }
    }

    private void assertLoadBadInput() throws Exception {
        val records = getCredentialRegistration("casuser");
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getRest();
        val port = URI.create(props.getUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource("_-@@-_".getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUserHandle(records.getUserIdentity().getId());
            assertTrue(registration.isEmpty());
        }
    }

    private void assertRegistrationBadStatus() {
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getRest();
        val port = URI.create(props.getUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.NOT_FOUND)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUsername("casuser");
            assertTrue(registration.isEmpty());
        }
    }

    private void assertLoadBadStatus() throws Exception {
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getRest();
        val port = URI.create(props.getUrl()).getPort();
        val records = getCredentialRegistration("casuser");
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.NOT_FOUND)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUserHandle(records.getUserIdentity().getId());
            assertTrue(registration.isEmpty());
        }
    }

    private void assertRegistrationIsFound() throws Exception {
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getRest();
        val port = URI.create(props.getUrl()).getPort();
        val records = getCredentialRegistration("casuser");
        val results = cipherExecutor.encode(WebAuthnUtils.getObjectMapper()
            .writeValueAsString(CollectionUtils.wrapList(records)));
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUsername("casuser");
            assertFalse(registration.isEmpty());
        }
    }

    private void assertLoadIsFound() throws Exception {
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getRest();
        val port = URI.create(props.getUrl()).getPort();
        val records = getCredentialRegistration("casuser");
        val results = cipherExecutor.encode(WebAuthnUtils.getObjectMapper()
            .writeValueAsString(CollectionUtils.wrapList(records)));
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "REST Output"), HttpStatus.OK)) {
            webServer.start();
            val registration = webAuthnCredentialRepository.getRegistrationsByUserHandle(records.getUserIdentity().getId());
            assertFalse(registration.isEmpty());
        }
    }
}
