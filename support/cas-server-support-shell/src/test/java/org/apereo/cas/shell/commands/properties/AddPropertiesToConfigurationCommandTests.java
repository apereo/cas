package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AddPropertiesToConfigurationCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class AddPropertiesToConfigurationCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() throws Exception {
        var file = File.createTempFile("cas", ".properties");
        exportProperties(file);
        file = File.createTempFile("cas", ".yml");
        exportProperties(file);
    }

    private void exportProperties(final File file) {
        assertDoesNotThrow(() -> shell.evaluate(() -> "add-properties --file " + file + " --group cas.server"));
        assertTrue(file.exists());
        file.deleteOnExit();
    }
}

