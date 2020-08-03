package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptEncryptPropertyCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class JasyptEncryptPropertyCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(() -> shell.evaluate(() -> "encrypt-value --value SOMEVALUE --password "
            + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }
}

