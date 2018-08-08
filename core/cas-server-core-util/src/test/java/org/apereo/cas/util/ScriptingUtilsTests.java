package org.apereo.cas.util;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link ScriptingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ScriptingUtilsTests {

    @Test
    public void verifyInlineGroovyScript() {
        assertTrue(ScriptingUtils.isInlineGroovyScript("groovy {return 0}"));
    }

    @Test
    public void verifyExternalGroovyScript() {
        assertTrue(ScriptingUtils.isExternalGroovyScript("file:/tmp/sample.groovy"));
    }

    @Test
    public void verifyGroovyScriptShellExecution() {
        val result = ScriptingUtils.executeGroovyShellScript("return name", CollectionUtils.wrap("name", "casuser"), String.class);
        assertEquals("casuser", result.toString());
    }

    @Test
    public void verifyGroovyResourceExecution() {
        try {
            val file = File.createTempFile("test", ".groovy");
            FileUtils.write(file, "def process(String name) { return name }", StandardCharsets.UTF_8);
            val resource = new FileSystemResource(file);

            val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
            assertEquals("casuser", result.toString());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyGroovyResourceEngineExecution() {
        val result = ScriptingUtils.executeGroovyScriptEngine("return name", CollectionUtils.wrap("name", "casuser"), String.class);
        assertEquals("casuser", result.toString());
    }

    @Test
    public void verifyResourceScriptEngineExecution() {
        try {
            val file = File.createTempFile("test", ".groovy");
            FileUtils.write(file, "def run(String name) { return name }", StandardCharsets.UTF_8);

            val result = ScriptingUtils.executeScriptEngine(file.getCanonicalPath(), new Object[]{"casuser"}, String.class);
            assertEquals("casuser", result.toString());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
