package org.apereo.cas.shell.commands.properties;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ExportPropertiesCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SHELL")
class ExportPropertiesCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> "export-props --dir=/tmp"));
    }
}
