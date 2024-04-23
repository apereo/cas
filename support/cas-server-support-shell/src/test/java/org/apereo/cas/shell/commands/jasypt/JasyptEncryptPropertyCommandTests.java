package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptEncryptPropertyCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class JasyptEncryptPropertyCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() throws Throwable {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "encrypt-value --value SOMEVALUE --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    void verifyFileEncryption() throws Throwable {
        val file = File.createTempFile("file", ".txt");
        FileUtils.write(file, UUID.randomUUID().toString(), StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "encrypt-value --file " + file.getAbsolutePath() + " --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    void verifyOperationWithInitVector() throws Throwable {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "encrypt-value --value SOMEVALUE --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }
}

