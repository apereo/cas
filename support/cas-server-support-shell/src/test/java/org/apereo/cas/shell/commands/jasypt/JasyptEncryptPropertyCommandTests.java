package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "encrypt-value --value SOMEVALUE --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    void verifyFileEncryption() throws Throwable {
        val file = Files.createTempFile("file", ".txt").toFile();
        FileUtils.write(file, UUID.randomUUID().toString(), StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "encrypt-value --file " + file.getAbsolutePath() + " --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    void verifyOperationWithInitVector() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "encrypt-value --value SOMEVALUE --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }
}

