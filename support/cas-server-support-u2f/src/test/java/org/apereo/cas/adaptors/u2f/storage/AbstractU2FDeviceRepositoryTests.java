package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.crypto.CertUtils;

import com.yubico.u2f.data.DeviceRegistration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractU2FDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@DirtiesContext
public abstract class AbstractU2FDeviceRepositoryTests {
    private static final String CASUSER = "casuser";

    @AfterEach
    public void tearDown() throws Exception {
        val deviceRepository = getDeviceRepository();
        deviceRepository.removeAll();
    }

    @Test
    public void verifyDeviceSaved() {
        val deviceRepository = getDeviceRepository();
        registerDevices(deviceRepository);
        val devices = deviceRepository.getRegisteredDevices(CASUSER);
        assertEquals(2, devices.size());
        deviceRepository.clean();
    }

    @SneakyThrows
    protected void registerDevices(final U2FDeviceRepository deviceRepository) {
        val cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
        val r1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
        val r2 = new DeviceRegistration("keyhandle22", "publickey1", cert, 2);
        deviceRepository.registerDevice(CASUSER, r1);
        deviceRepository.registerDevice(CASUSER, r2);
        deviceRepository.authenticateDevice(CASUSER, r1);
        assertTrue(deviceRepository.isDeviceRegisteredFor(CASUSER));
    }

    protected abstract U2FDeviceRepository getDeviceRepository();
}
