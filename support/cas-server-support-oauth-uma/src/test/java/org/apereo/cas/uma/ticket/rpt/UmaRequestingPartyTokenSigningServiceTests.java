package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaRequestingPartyTokenSigningServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
public class UmaRequestingPartyTokenSigningServiceTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyUnknownJwks() {
        val jwks = new ClassPathResource("nothing.jwks");
        val signingService = new UmaRequestingPartyTokenSigningService(jwks, "cas");
        assertNull(signingService.getJsonWebKeySigningKey());
    }

    @Test
    public void verifyEmptyJwks() throws Exception {
        val file = File.createTempFile("uma-keystore", ".jwks");
        FileUtils.write(file, "{\"keys\": []}", StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class,
            () -> new UmaRequestingPartyTokenSigningService(new FileSystemResource(file), "cas"));
    }

}
