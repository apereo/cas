package org.apereo.cas.qr.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class QRAuthenticationDeviceRepositoryTests {
    @Test
    public void verifyPermitAll() {
        val permitAll = QRAuthenticationDeviceRepository.permitAll();
        permitAll.removeDevice(UUID.randomUUID().toString());
        permitAll.removeAll();
        permitAll.authorizeDeviceFor("any", "any");
        assertTrue(permitAll.getAuthorizedDevicesFor("any").isEmpty());
        assertTrue(permitAll.isAuthorizedDeviceFor("anything", "anyuser"));
    }

}
