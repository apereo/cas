package org.apereo.cas.util;

import org.apereo.cas.util.scripting.ScriptingUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScriptingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ScriptingUtilsTests {

    @Test
    void verifyInlineGroovyScript() {
        assertTrue(ScriptingUtils.isInlineGroovyScript("groovy {return 0}"));
    }

    @Test
    void verifyExternalGroovyScript() {
        assertTrue(ScriptingUtils.isExternalGroovyScript("file:/tmp/sample.groovy"));
    }

    @Test
    void verifyGroovyScriptShellExecution() {
        val script = ScriptingUtils.parseGroovyShellScript("return name");
        val result = ScriptingUtils.executeGroovyShellScript(script, CollectionUtils.wrap("name", "casuser"), String.class);
        assertEquals("casuser", result);
    }

    @Test
    @SneakyThrows
    void verifyGroovyResourceFileExecution() {
        val file = File.createTempFile("test", ".groovy");
        FileUtils.write(file, "def process(String name) { return name }", StandardCharsets.UTF_8);
        val resource = new FileSystemResource(file);

        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertEquals("casuser", result);
    }

    @Test
    @SneakyThrows
    void verifyGroovyResourceClasspathExecution() {
        try {
            val resource = new ClassPathResource("ScriptingUtilsTestGroovyScript.groovy");

            val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
            assertEquals("casuser", result);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    void verifyGroovyResourceEngineExecution() {
        val result = ScriptingUtils.executeGroovyScriptEngine("return name", CollectionUtils.wrap("name", "casuser"), String.class);
        assertEquals("casuser", result);
    }

    @Test
    @SneakyThrows
    void verifyResourceScriptEngineExecution() {
        val file = File.createTempFile("test", ".groovy");
        FileUtils.write(file, "def run(String name) { return name }", StandardCharsets.UTF_8);

        val result = ScriptingUtils.executeScriptEngine(file.getCanonicalPath(), new Object[]{"casuser"}, String.class);
        assertEquals("casuser", result);
    }
}
