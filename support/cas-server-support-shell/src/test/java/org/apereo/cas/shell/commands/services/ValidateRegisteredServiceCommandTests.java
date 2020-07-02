package org.apereo.cas.shell.commands.services;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateRegisteredServiceCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class ValidateRegisteredServiceCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() throws Exception {
        val file = File.createTempFile("service", ".json");
        val yaml = File.createTempFile("service", ".yaml");
        val svc = RegisteredServiceTestUtils.getRegisteredService("example");

        try (val writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            new RegisteredServiceJsonSerializer().to(writer, svc);
            writer.flush();
        }
        assertTrue(file.exists() && file.length() > 0);
        assertNotNull(shell.evaluate(() -> "generate-yaml --file " + file.getPath() + " --destination " + yaml.getPath()));
        assertTrue(yaml.exists());

        assertDoesNotThrow(() -> shell.evaluate(() -> "validate-service --file " + file.getPath()));
        assertDoesNotThrow(() -> shell.evaluate(() -> "validate-service --file " + yaml.getPath()));
        assertDoesNotThrow(() -> shell.evaluate(() -> "validate-service --directory " + file.getParent()));
    }
}

