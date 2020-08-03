package org.apereo.cas.adaptors.u2f.web;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FWebflowConfiguration;
import org.apereo.cas.config.support.authentication.U2FAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.config.support.authentication.U2FAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.support.authentication.U2FAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import com.yubico.u2f.data.DeviceRegistration;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FRegisteredDevicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = "management.endpoint.u2fDevices.enabled=true")
@Tag("ActuatorEndpoint")
@Import({
    U2FConfiguration.class,
    U2FAuthenticationComponentSerializationConfiguration.class,
    U2FAuthenticationEventExecutionPlanConfiguration.class,
    U2FAuthenticationMultifactorProviderBypassConfiguration.class,
    U2FWebflowConfiguration.class
})
public class U2FRegisteredDevicesEndpointTests extends AbstractCasEndpointTests {

    @Autowired
    @Qualifier("u2fRegisteredDevicesEndpoint")
    private U2FRegisteredDevicesEndpoint endpoint;

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;

    @BeforeEach
    public void beforeEach() throws Exception {
        deviceRepository.removeAll();
    }

    @Test
    public void verifyOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
        val r1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
        var record = U2FDeviceRegistration.builder()
            .record(deviceRepository.getCipherExecutor().encode(r1.toJsonWithAttestationCert()))
            .username(id)
            .build();
        record = deviceRepository.registerDevice(record);
        assertFalse(endpoint.fetchAll().isEmpty());
        assertFalse(endpoint.fetchBy(id).isEmpty());

        endpoint.delete(id, record.getId());
        endpoint.delete(id);
        assertTrue(endpoint.fetchAll().isEmpty());
    }
}
