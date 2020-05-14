package org.apereo.cas.shell.commands.services;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateYamlRegisteredServiceCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class GenerateYamlRegisteredServiceCommandTests extends BaseCasShellCommandTests {
    @Test
    @SneakyThrows
    public void verifyOperation() {
        val file = File.createTempFile("service", ".json");
        val yaml = File.createTempFile("service", ".yaml");
        val svc = RegisteredServiceTestUtils.getRegisteredService("example");
        new RegisteredServiceJsonSerializer().to(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8), svc);
        assertTrue(file.exists());
        assertNotNull(shell.evaluate(() -> "generate-yaml --file " + file.getPath() + " --destination " + yaml.getPath()));
        assertTrue(yaml.exists());
    }
}
