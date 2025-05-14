package org.apereo.cas.util;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
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
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScriptingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
class ScriptingUtilsTests {

    @Test
    void verifyInlineGroovyScript() {
        assertTrue(ScriptingUtils.isInlineGroovyScript("groovy {return 0}"));
        val script = ScriptingUtils.parseGroovyShellScript("return authentication.principal.id + ' @ ' + authentication.authenticationDate");
        val authn = mock(Authentication.class);
        when(authn.getAuthenticationDate()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("casuser");
        when(authn.getPrincipal()).thenReturn(principal);
        val result = ScriptingUtils.executeGroovyShellScript(script, Map.of("authentication", authn), String.class);
        assertTrue(Objects.requireNonNull(result).startsWith("casuser"));
    }

    @Test
    void verifyExternalGroovyScript() {
        assertTrue(ScriptingUtils.isExternalGroovyScript("file:/somefolder/sample.groovy"));
    }

    @Test
    void verifyGroovyScriptShellExecution() {
        val script = ScriptingUtils.parseGroovyShellScript("return name");
        val result = ScriptingUtils.executeGroovyShellScript(script, CollectionUtils.wrap("name", "casuser"), String.class);
        assertEquals("casuser", result);
    }

    @Test
    void verifyGroovyExecutionFails() {
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
    void verifyGroovyResourceFileExecution() throws Throwable {
        val file = File.createTempFile("test", ".groovy");
        FileUtils.write(file, "def process(String name) { return name }", StandardCharsets.UTF_8);
        val resource = new FileSystemResource(file);

        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertEquals("casuser", result);
    }

    @Test
    void verifyGroovyReturnTypeMismatch() throws Throwable {
        val file = File.createTempFile("test", ".groovy");
        FileUtils.write(file, "def process(String name) { return name }", StandardCharsets.UTF_8);
        val resource = new FileSystemResource(file);
        assertNull(ScriptingUtils.getObjectInstanceFromGroovyResource(resource,
            ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_OBJECT_ARRAY,
            Map.class));
    }

    @Test
    void verifyGroovyResourceFileNotFound() {
        val resource = new FileSystemResource(new File("missing.groovy"));

        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertNull(result);
    }

    @Test
    void verifyGroovyResourceClasspathExecution() {
        val resource = new ClassPathResource("ScriptingUtilsTestGroovyScript.groovy");

        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertEquals("casuser", result);
    }

    @Test
    void verifyGroovyResourceClasspathNotFound() {
        val resource = new ClassPathResource("missing.groovy");
        val result = ScriptingUtils.executeGroovyScript(resource, "process", String.class, "casuser");
        assertNull(result);
    }

    @Test
    void verifyGetObject() {
        var result = ScriptingUtils.getObjectInstanceFromGroovyResource(null, null, null, null);
        assertNull(result);
        result = ScriptingUtils.getObjectInstanceFromGroovyResource(mock(Resource.class), null, null, null);
        assertNull(result);
    }
}
