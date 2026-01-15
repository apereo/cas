package org.apereo.cas.shell.commands.jasypt;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptListAlgorithmsCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class JasyptListAlgorithmsCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> "jasypt-list-algorithms --includeBC=true"));
    }

    @Test
    void verifyNoBouncyCastleOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> "jasypt-list-algorithms"));
    }
}
