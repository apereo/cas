package org.apereo.cas.shell.commands.cipher;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StringableCipherExecutorCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class StringableCipherExecutorCommandTests extends BaseCasShellCommandTests {
    private static final String SAMPLE_ENCRYPTION_KEY = "AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M";

    private static final String SAMPLE_SIGNING_KEY = "cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ";

    @Test
    void verifyOperation() throws Throwable {
        val result = assertDoesNotThrow(() -> runShellCommand(() -> () -> "cipher-text --value example --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY));
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "decipher-text --value " + result + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY));
    }

    @Test
    void verifyFile() throws Throwable {
        val file = File.createTempFile("file", "txt");
        FileUtils.write(file, "example", StandardCharsets.UTF_8);

        val path = file.getCanonicalPath();
        var result = assertDoesNotThrow(() -> runShellCommand(
            () -> () -> "cipher-text --value " + path + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY));
        FileUtils.write(file, result.toString(), StandardCharsets.UTF_8);

        result = assertDoesNotThrow(() -> runShellCommand(
            () -> () -> "decipher-text --value " + path + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY));
        assertEquals("example", result.toString());
    }
}

