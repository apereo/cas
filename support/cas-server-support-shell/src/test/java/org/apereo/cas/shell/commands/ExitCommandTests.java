package org.apereo.cas.shell.commands;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ExitCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class ExitCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        shell.evaluate(() -> "quit");
        fail("Shell should have quit but did not");
    }
}
