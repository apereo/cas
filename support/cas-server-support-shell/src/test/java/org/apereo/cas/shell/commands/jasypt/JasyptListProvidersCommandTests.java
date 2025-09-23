package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptListProvidersCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class JasyptListProvidersCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "jasypt-list-providers --includeBC"));
    }

    @Test
    void verifyNoBouncyCastleOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "jasypt-list-providers"));
    }
}

