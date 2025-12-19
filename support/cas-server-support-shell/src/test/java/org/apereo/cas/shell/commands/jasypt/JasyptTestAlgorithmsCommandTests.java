package org.apereo.cas.shell.commands.jasypt;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptTestAlgorithmsCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class JasyptTestAlgorithmsCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> "jasypt-test-algorithms"));
    }
}
