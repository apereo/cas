package org.apereo.cas.trusted.web;

import org.apereo.cas.config.CasMultifactorAuthnTrustAutoConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationTrustedDevicesReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.multifactorTrustedDevices.enabled=true")
@Tag("MFATrustedDevices")
@ImportAutoConfiguration(CasMultifactorAuthnTrustAutoConfiguration.class)
class MultifactorAuthenticationTrustedDevicesReportEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("mfaTrustedDevicesReportEndpoint")
    private MultifactorAuthenticationTrustedDevicesReportEndpoint endpoint;

    @Autowired
    @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Test
    void verifyRemovals() throws Throwable {
        assertNotNull(endpoint);
        var record = MultifactorAuthenticationTrustRecord.newInstance(UUID.randomUUID().toString(), "geography", "fingerprint");
        mfaTrustEngine.save(record);
        endpoint.clean();
        endpoint.removeSince(DateTimeUtils.dateOf(LocalDateTime.now(Clock.systemUTC()).plusDays(1)));
        assertFalse(endpoint.devices().isEmpty());
    }

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(endpoint);
        var record = MultifactorAuthenticationTrustRecord.newInstance(UUID.randomUUID().toString(), "geography", "fingerprint");
        record = mfaTrustEngine.save(record);
        assertFalse(endpoint.devices().isEmpty());
        assertFalse(endpoint.devicesForUser(record.getPrincipal()).isEmpty());

        endpoint.revoke(record.getRecordKey());
        assertTrue(endpoint.devices().isEmpty());
        assertTrue(endpoint.devicesForUser(record.getPrincipal()).isEmpty());
    }

    @Test
    void verifyImportExport() throws Throwable {
        var record = MultifactorAuthenticationTrustRecord.newInstance(
            UUID.randomUUID().toString(), "london", "fingerprint");
        val request = new MockHttpServletRequest();
        val content = MAPPER.writeValueAsString(record);
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importDevice(request).getStatusCode());
        var entity = endpoint.export();
        assertEquals("attachment", entity.getHeaders().getContentDisposition().getType());
        entity = endpoint.exportUserDevices(record.getPrincipal());
        assertEquals("attachment", entity.getHeaders().getContentDisposition().getType());
    }
}
