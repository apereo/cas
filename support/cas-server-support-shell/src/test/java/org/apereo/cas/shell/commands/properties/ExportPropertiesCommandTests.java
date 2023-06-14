package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ExportPropertiesCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
class ExportPropertiesCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "export-props --dir /tmp"));
    }
}
