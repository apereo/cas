package org.apereo.cas.shell.commands.util;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateEndpointCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class ValidateEndpointCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        val result = shell.evaluate(
            () -> "validate-endpoint --url https://github.com");
        assertTrue((Boolean) result);

        val result2 = shell.evaluate(
            () -> "validate-endpoint --timeout 1000 --url https://wrong.host.badssl.com/");
        assertFalse((Boolean) result2);
    }
}

