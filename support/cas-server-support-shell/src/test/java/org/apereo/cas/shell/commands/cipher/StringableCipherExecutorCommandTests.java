package org.apereo.cas.shell.commands.cipher;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StringableCipherExecutorCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class StringableCipherExecutorCommandTests extends BaseCasShellCommandTests {
    private static final String ENCRYPTION_ALG = ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;
    private static final String SAMPLE_ENCRYPTION_KEY = "AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M";
    private static final String SAMPLE_SIGNING_KEY = "cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ";

    @Test
    void verifyOperation() {
        val result = assertDoesNotThrow(() -> runShellCommand(() ->
            () -> "cipher-text --value example --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY) + " --encryption-alg " + ENCRYPTION_ALG);
        assertDoesNotThrow(() -> runShellCommand(() ->
            () -> "decipher-text --value " + result + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY + " --encryption-alg " + ENCRYPTION_ALG));
    }

    @Test
    void verifyFile() throws Throwable {
        val file = Files.createTempFile("file", "txt").toFile();
        FileUtils.write(file, "example", StandardCharsets.UTF_8);

        val path = file.getCanonicalPath();
        var result = assertDoesNotThrow(() -> runShellCommand(
            () -> () -> "cipher-text --value " + path + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY + " --encryption-alg " + ENCRYPTION_ALG));
        FileUtils.write(file, result.toString(), StandardCharsets.UTF_8);

        result = assertDoesNotThrow(() -> runShellCommand(
            () -> () -> "decipher-text --value " + path + " --encryption-key " + SAMPLE_ENCRYPTION_KEY + " --signing-key " + SAMPLE_SIGNING_KEY + " --encryption-alg " + ENCRYPTION_ALG));
        assertEquals("example", result.toString());
    }
}

