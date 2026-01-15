package org.apereo.cas.shell.commands.properties;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.cryptacular.io.ClassPathResource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConvertPropertiesToYAMLCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("SHELL")
class ConvertPropertiesToYAMLCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() throws Throwable {
        val properties = new Properties();
        properties.load(new ClassPathResource("testconfig.properties").getInputStream());
        val tempFile = Files.createTempFile("casconfig", ".properties");
        properties.store(new FileWriter(tempFile.toFile(), StandardCharsets.UTF_8), "CAS Configuration");
        assertDoesNotThrow(() -> runShellCommand(() -> "convert-props --properties=" + tempFile.toFile().getAbsolutePath()));
        assertTrue(Files.exists(tempFile.getParent().resolve(FilenameUtils.getBaseName(tempFile.toFile().getName()) + ".yml")));
    }
}

