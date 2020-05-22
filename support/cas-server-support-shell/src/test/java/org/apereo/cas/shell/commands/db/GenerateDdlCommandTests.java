package org.apereo.cas.shell.commands.db;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateDdlCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class GenerateDdlCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() throws Exception {
        val file = File.createTempFile("ddl", "sql");
        val result = shell.evaluate(
            () -> "generate-ddl --file " + file + " --dialect HSQL");
        assertNotNull(result);
    }
}

