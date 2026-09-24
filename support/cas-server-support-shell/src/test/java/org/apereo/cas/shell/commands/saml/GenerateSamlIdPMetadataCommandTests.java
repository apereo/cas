package org.apereo.cas.shell.commands.saml;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateSamlIdPMetadataCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class GenerateSamlIdPMetadataCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() throws Exception {
        val location = Files.createTempDirectory("samlidp").toFile().getAbsolutePath();
        assertDoesNotThrow(() -> runShellCommand(() -> "generate-idp-metadata --force=true --metadataLocation="
            + location + " --subjectAltNames=helloworld"));
    }
}

