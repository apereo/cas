package org.apereo.cas.shell.commands.db;

import java.nio.file.Files;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateDdlCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class GenerateDdlCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() throws Throwable {
        val file = Files.createTempFile("ddl", "sql").toFile();
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --createSchema --dropSchema --file " + file + " --dialect HSQL"));
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --file " + file + " --dialect HSQL"));
    }

    @Test
    void verifyCreateOperation() throws Throwable {
        val file = Files.createTempFile("ddl", "sql").toFile();
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --createSchema --file " + file + " --dialect HSQL"));
    }

    @Test
    void verifyDropOperation() throws Throwable {
        val file = Files.createTempFile("ddl", "sql").toFile();
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --url jdbc:hsqldb:mem:cas2 --dropSchema --file " + file + " --dialect HSQL"));
    }

    @Test
    void verifyBadDialect() throws Throwable {
        val file = Files.createTempFile("ddl", "sql").toFile();
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --file " + file + " --dialect XYZ"));
    }
}

