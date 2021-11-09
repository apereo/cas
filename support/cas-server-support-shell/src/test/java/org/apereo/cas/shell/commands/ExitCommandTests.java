package org.apereo.cas.shell.commands;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ExitCommandTests}.
 * This should be run last using a high order number
 * to allow for all other tests to pass. Successful execution
 * of this test class would terminate the runtime.
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
@Order(Order.DEFAULT + 1)
public class ExitCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        shell.evaluate(() -> "quit");
        fail("Shell should have quit but did not");
    }
}
