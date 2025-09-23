package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AddPropertiesToConfigurationCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class AddPropertiesToConfigurationCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() throws Throwable {
        var file = Files.createTempFile("cas", ".properties").toFile();
        exportProperties(file);
        file = Files.createTempFile("cas", ".yml").toFile();
        exportProperties(file);
    }

    private void exportProperties(final File file) {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "add-properties --file " + file + " --group cas.server"));
        assertTrue(file.exists());
        file.deleteOnExit();
    }
}

