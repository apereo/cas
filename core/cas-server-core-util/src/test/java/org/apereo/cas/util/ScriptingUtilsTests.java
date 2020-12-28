package org.apereo.cas.util;

import org.apereo.cas.util.scripting.ScriptingUtils;

import groovy.lang.Script;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScriptingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
public class ScriptingUtilsTests {

    @Test
    public void verifyInlineGroovyScript() {
        assertTrue(ScriptingUtils.isInlineGroovyScript("groovy {return 0}"));
    }

    @Test
    public void verifyExternalGroovyScript() {
        assertTrue(ScriptingUtils.isExternalGroovyScript("file:/somefolder/sample.groovy"));
    }

    @Test
    public void verifyGroovyScriptShellExecution() {
        val script = ScriptingUtils.parseGroovyShellScript("return name");
        val result = ScriptingUtils.executeGroovyShellScript(script, CollectionUtils.wrap("name", "casuser"), String.class);
        assertEquals("casuser", result);
    }

    @Test
    public void verifyGroovyExecutionFails() {
        var result = ScriptingUtils.executeGroovyShellScript(mock(Script.class), CollectionUtils.wrap("name", "casuser"), String.class);
        assertNull(result);

        result = ScriptingUtils.executeGroovyScript(mock(Resource.class), "someMethod", String.class);
        assertNull(result);

        result = ScriptingUtils.executeGroovyScript(mock(Resource.class), null, String.class);
        assertNull(result);

        assertNull(ScriptingUtils.parseGroovyShellScript(null));

        assertThrows(RuntimeException.class,
            () -> ScriptingUtils.executeGroovyScript(mock(Resource.class), "someMethod",
                ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true));
    }

    @Test
    public void verifyGroovyResourceFileExecution() throws IOException {
        val file = File.createTempFile("test", ".groovy");
        FileUtils.write(file, "def process(String name) { return name }", StandardCharsets.UTF_8);
        val resource = new FileSystemResource(file);

        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertEquals("casuser", result);
    }

    @Test
    public void verifyGroovyReturnTypeMismatch() throws IOException {
        val file = File.createTempFile("test", ".groovy");
        FileUtils.write(file, "def process(String name) { return name }", StandardCharsets.UTF_8);
        val resource = new FileSystemResource(file);
        assertNull(ScriptingUtils.getObjectInstanceFromGroovyResource(resource,
            ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_OBJECT_ARRAY,
            Map.class));
    }

    @Test
    public void verifyGroovyResourceFileNotFound() {
        val resource = new FileSystemResource(new File("missing.groovy"));

        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertNull(result);
    }

    @Test
    public void verifyGroovyResourceClasspathExecution() {
        val resource = new ClassPathResource("ScriptingUtilsTestGroovyScript.groovy");

        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertEquals("casuser", result);
    }

    @Test
    public void verifyGroovyResourceClasspathNotFound() {
        val resource = new ClassPathResource("missing.groovy");
        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertNull(result);
    }

    @Test
    public void verifyGroovyResourceEngineExecution() {
        val result = ScriptingUtils.executeGroovyScriptEngine("return name", CollectionUtils.wrap("name", "casuser"), String.class);
        assertEquals("casuser", result);

        val result2 = ScriptingUtils.executeGroovyScriptEngine("throw new RuntimeException()", Map.of(), String.class);
        assertNull(result2);
    }
    
    @Test
    public void verifyResourceScriptEngineExecution() throws IOException {
        val file = File.createTempFile("test", ".groovy");
        FileUtils.write(file, "def run(String name) { return name }", StandardCharsets.UTF_8);

        val result = ScriptingUtils.executeScriptEngine(file.getCanonicalPath(), new Object[]{"casuser"}, String.class);
        assertEquals("casuser", result);
    }

    @Test
    public void verifyBadScriptEngine() throws IOException {
        val file = File.createTempFile("test1", ".groovy");
        FileUtils.write(file, "---", StandardCharsets.UTF_8);
        val result = ScriptingUtils.executeScriptEngine(file.getCanonicalPath(), new Object[]{"casuser"}, String.class);
        assertNull(result);
    }

    @Test
    public void verifyEmptyScript() throws IOException {
        val result = ScriptingUtils.executeScriptEngine(new File("bad.groovy").getCanonicalPath(), new Object[]{"casuser"}, String.class);
        assertNull(result);
    }

    @Test
    public void verifyNoEngine() throws IOException {
        val file = File.createTempFile("test", ".txt");
        FileUtils.write(file, "-", StandardCharsets.UTF_8);
        val result = ScriptingUtils.executeScriptEngine(file.getCanonicalPath(), new Object[]{"casuser"}, String.class);
        assertNull(result);
    }

    @Test
    public void verifyGetObject() {
        var result = ScriptingUtils.getObjectInstanceFromGroovyResource(null, null, null, null);
        assertNull(result);
        result = ScriptingUtils.getObjectInstanceFromGroovyResource(mock(Resource.class), null, null, null);
        assertNull(result);
    }
}
