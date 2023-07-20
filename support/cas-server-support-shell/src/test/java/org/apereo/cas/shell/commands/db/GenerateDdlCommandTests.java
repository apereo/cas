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
class GenerateDdlCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() throws Exception {
        val file = File.createTempFile("ddl", "sql");
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --createSchema --dropSchema --file " + file + " --dialect HSQL"));
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --file " + file + " --dialect HSQL"));
    }

    @Test
    void verifyCreateOperation() throws Exception {
        val file = File.createTempFile("ddl", "sql");
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --createSchema --file " + file + " --dialect HSQL"));
    }

    @Test
    void verifyDropOperation() throws Exception {
        val file = File.createTempFile("ddl", "sql");
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --url jdbc:hsqldb:mem:cas2 --dropSchema --file " + file + " --dialect HSQL"));
    }

    @Test
    void verifyBadDialect() throws Exception {
        val file = File.createTempFile("ddl", "sql");
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-ddl --file " + file + " --dialect XYZ"));
    }
}

