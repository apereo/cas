package org.apereo.cas.shell.commands.jwt;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateJwtCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class GenerateJwtCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> "generate-jwt --subject=casuser"));
        assertThrows(IllegalArgumentException.class, () -> new GenerateJwtCommand().generate(256, 48, "HS256", "dir", "A192CBC-HS384", " "));
    }

    /**
     * Without a signing algorithm or secret size the JWT would be unsigned, which CAS does not support, so it is refused;
     * without encryption the JWT is only signed.
     */
    @Test
    void verifyBadSize() {
        val command = new GenerateJwtCommand();
        assertThrows(IllegalArgumentException.class, () -> command.generate(-1, 48, "HS256", "dir", "A192CBC-HS384", "casuser"));
        assertThrows(IllegalArgumentException.class, () -> command.generate(256, 48, " ", "dir", "A192CBC-HS384", "casuser"));
        assertThrows(IllegalArgumentException.class, () -> command.generate(256, 48, "none", "dir", "A192CBC-HS384", "casuser"));
        assertDoesNotThrow(() -> command.generate(256, -1, "HS256", "dir", "A192CBC-HS384", "casuser"));
        assertDoesNotThrow(() -> runShellCommand(() -> "generate-jwt --subject=casuser --encryptionSecretSize=-1"));
    }

    @Test
    void verifyBadAlg() {
        assertDoesNotThrow(() -> runShellCommand(() -> "generate-jwt --subject=casuser --encryptionAlgorithm=dir --encryptionMethod=A128KW"));
        assertDoesNotThrow(
            () -> runShellCommand(() -> "generate-jwt --subject=casuser --encryptionAlgorithm=A128KW --encryptionMethod=A128CBC_HS256"));

    }

}

