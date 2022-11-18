package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

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
@EnableAutoConfiguration
@Tag("SHELL")
public class JasyptEncryptPropertyCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(() -> shell.run(() -> () -> "encrypt-value --value SOMEVALUE --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    public void verifyFileEncryption() throws Exception {
        val file = File.createTempFile("file", ".txt");
        FileUtils.write(file, UUID.randomUUID().toString(), StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> shell.run(() -> () -> "encrypt-value --file " + file.getAbsolutePath() + " --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    public void verifyOperationWithInitVector() {
        assertDoesNotThrow(() -> shell.run(() -> () -> "encrypt-value --value SOMEVALUE --password "
                                                      + "JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }
}

