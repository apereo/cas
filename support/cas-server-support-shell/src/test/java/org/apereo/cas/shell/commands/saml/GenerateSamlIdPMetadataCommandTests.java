package org.apereo.cas.shell.commands.saml;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateSamlIdPMetadataCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class GenerateSamlIdPMetadataCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        val location = FileUtils.getTempDirectoryPath();
        assertDoesNotThrow(() -> shell.evaluate(() -> "generate-idp-metadata --force true --metadataLocation "
            + location + " --subjectAltNames helloworld"));
    }
}

