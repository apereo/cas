package org.apereo.cas.adaptors.u2f.storage;

import com.yubico.u2f.data.DeviceRegistration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.crypto.CertUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractU2FDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@DirtiesContext
public abstract class AbstractU2FDeviceRepositoryTests {

    @Test
    public void verifyDeviceSaved() {
        try {
            registerDevices();
            final var deviceRepository = getDeviceRepository();
            final var devs = deviceRepository.getRegisteredDevices("casuser");
            verifyDevicesAvailable(devs);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @SneakyThrows
    protected void registerDevices() {
        final var cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
        final var r1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
        final var r2 = new DeviceRegistration("keyhandle22", "publickey1", cert, 2);
        final var deviceRepository = getDeviceRepository();
        deviceRepository.registerDevice("casuser", r1);
        deviceRepository.registerDevice("casuser", r2);
    }

    protected void verifyDevicesAvailable(final Collection<DeviceRegistration> devs) {
        assertEquals(2, devs.size());
    }

    protected abstract U2FDeviceRepository getDeviceRepository();
}
