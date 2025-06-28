package org.apereo.cas.shell.commands.services;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

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
@Tag("SHELL")
class GenerateYamlRegisteredServiceCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() throws Throwable {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val file = Files.createTempFile("service", ".json").toFile();
        val yaml = Files.createTempFile("service", ".yaml").toFile();
        val svc = RegisteredServiceTestUtils.getRegisteredService("example");
        try (val writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            new RegisteredServiceJsonSerializer(appCtx).to(writer, svc);
        }
        assertTrue(file.exists() && file.length() > 0);
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-yaml --file " + file.getPath() + " --destination " + yaml.getPath()));
        assertTrue(yaml.exists());
        val badFile = Files.createTempFile("first", ".second").toFile();
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-yaml --file " + badFile + " --destination " + yaml.getPath()));
        FileUtils.write(badFile, "data", StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-yaml --file " + badFile + " --destination " + yaml.getPath()));
    }
}
