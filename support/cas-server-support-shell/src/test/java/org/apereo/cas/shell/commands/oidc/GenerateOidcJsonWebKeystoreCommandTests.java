package org.apereo.cas.shell.commands.oidc;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateOidcJsonWebKeystoreCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("SHELL")
class GenerateOidcJsonWebKeystoreCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        val location = FileUtils.getTempDirectoryPath();
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-oidc-jwks --jwksFile " + location + " --jwksKeyId cas"));
    }
}
