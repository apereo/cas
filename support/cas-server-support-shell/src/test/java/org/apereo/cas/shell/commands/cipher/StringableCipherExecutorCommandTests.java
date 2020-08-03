package org.apereo.cas.shell.commands.cipher;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StringableCipherExecutorCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class StringableCipherExecutorCommandTests extends BaseCasShellCommandTests {
    private static final String SAMPLE_ENCRYPTION_KEY = "AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M";

    private static final String SAMPLE_SIGNING_KEY = "cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ";

    @Test
    public void verifyOperation() {
        val result = shell.evaluate(
            () -> "cipher-text --value example --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY);
        assertNotNull(result);

        val decoded = shell.evaluate(
            () -> "decipher-text --value " + result + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY);
        assertEquals("example", decoded);
    }

    @Test
    public void verifyFile() throws Exception {
        val file = File.createTempFile("file", "txt");
        FileUtils.write(file, "example", StandardCharsets.UTF_8);

        val path = file.getCanonicalPath();
        val result = shell.evaluate(
            () -> "cipher-text --value " + path + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY);
        assertNotNull(result);
        FileUtils.write(file, result.toString(), StandardCharsets.UTF_8);

        val decoded = shell.evaluate(
            () -> "decipher-text --value " + path + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY);
        assertEquals("example", decoded);
    }
}

