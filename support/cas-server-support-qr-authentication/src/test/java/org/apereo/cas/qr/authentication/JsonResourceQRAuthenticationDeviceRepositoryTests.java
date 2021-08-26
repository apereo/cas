package org.apereo.cas.qr.authentication;

import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourceQRAuthenticationDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("FileSystem")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
public class JsonResourceQRAuthenticationDeviceRepositoryTests {
    @Test
    public void verifyOperation() throws Exception {
        val resource = new FileSystemResource(Files.createTempFile("devices", ".json"));
        val repo = new JsonResourceQRAuthenticationDeviceRepository(resource);

        val device1 = UUID.randomUUID().toString();
        val device2 = UUID.randomUUID().toString();
        repo.removeAll();
        repo.authorizeDeviceFor(device1, "casuser");
        repo.authorizeDeviceFor(device2, "casuser");
        assertTrue(repo.isAuthorizedDeviceFor(device2, "casuser"));
        assertEquals(2, repo.getAuthorizedDevicesFor("casuser").size());
        repo.removeDevice(device1);
        assertEquals(1, repo.getAuthorizedDevicesFor("casuser").size());
    }
}
