package org.apereo.cas.shell.commands.jwt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

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
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser"));
    }

    @Test
    void verifyBadSize() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser --signingSecretSize -1 "));
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser --encryptionSecretSize -1 "));
    }

    @Test
    void verifyBadAlg() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser --encryptionAlgorithm dir --encryptionMethod A128KW "));
        assertDoesNotThrow(
            () -> runShellCommand(() -> () -> "generate-jwt --subject casuser --encryptionAlgorithm A128KW --encryptionMethod A128CBC_HS256"));

    }

}

