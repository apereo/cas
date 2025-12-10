package org.apereo.cas.shell.commands.util;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateEndpointCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class ValidateEndpointCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> "validate-endpoint --url=http://http.badssl.com/"));
        assertDoesNotThrow(() -> runShellCommand(() -> "validate-endpoint --url=https://github.com"));
        assertDoesNotThrow(() -> runShellCommand(() -> "validate-endpoint --timeout=1000 --url=https://wrong.host.badssl.com/"));
        assertDoesNotThrow(() -> runShellCommand(() -> "validate-endpoint --url=https://self-signed.badssl.com"));
        assertDoesNotThrow(() -> runShellCommand(() -> "validate-endpoint --proxy=https://httpbin.org:443 --url=https://self-signed.badssl.com"));
    }
}

