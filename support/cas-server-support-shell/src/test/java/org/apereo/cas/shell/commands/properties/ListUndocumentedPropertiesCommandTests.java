package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ListUndocumentedPropertiesCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
public class ListUndocumentedPropertiesCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(() -> shell.evaluate(() -> "list-undocumented"));
    }
}

