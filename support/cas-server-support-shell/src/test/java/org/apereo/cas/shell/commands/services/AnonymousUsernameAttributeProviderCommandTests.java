package org.apereo.cas.shell.commands.services;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AnonymousUsernameAttributeProviderCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class AnonymousUsernameAttributeProviderCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        assertNotNull(shell.evaluate(() -> "generate-anonymous-user --username casuser --service example --salt ythr91%^1"));
    }
}
