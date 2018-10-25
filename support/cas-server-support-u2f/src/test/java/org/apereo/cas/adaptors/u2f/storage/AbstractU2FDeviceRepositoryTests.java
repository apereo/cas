package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.crypto.CertUtils;

import com.yubico.u2f.data.DeviceRegistration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractU2FDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@DirtiesContext
public abstract class AbstractU2FDeviceRepositoryTests {

    @Test
    public void verifyDeviceSaved() {
        try {
            val deviceRepository = getDeviceRepository();
            registerDevices(deviceRepository);
            val devs = deviceRepository.getRegisteredDevices("casuser");
            verifyDevicesAvailable(devs);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @SneakyThrows
    protected void registerDevices(final U2FDeviceRepository deviceRepository) {
        val cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
        val r1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
        val r2 = new DeviceRegistration("keyhandle22", "publickey1", cert, 2);
        deviceRepository.registerDevice("casuser", r1);
        deviceRepository.registerDevice("casuser", r2);
    }

    protected void verifyDevicesAvailable(final Collection<? extends DeviceRegistration> devs) {
        assertEquals(2, devs.size());
    }

    protected abstract U2FDeviceRepository getDeviceRepository();
}
