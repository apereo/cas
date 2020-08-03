package org.apereo.cas.shell.commands.cipher;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateCryptoKeysCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class GenerateCryptoKeysCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        val result = shell.evaluate(() -> "generate-key --key-size 512");
        assertNotNull(result);
    }
}
