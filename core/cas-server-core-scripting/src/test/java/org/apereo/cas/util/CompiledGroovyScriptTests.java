package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.scripting.ScriptingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.FileSystemResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompiledGroovyScriptTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Groovy")
class CompiledGroovyScriptTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "return 'cas'",
        "def run(Object... args) { args[0] }",
        "import groovy.transform.Field\n@Field final logger = org.slf4j.LoggerFactory.getLogger('cas')\nreturn 1",
        "import groovy.transform.Field\n@Field static final LIMIT = 10\nreturn LIMIT",
        "import groovy.transform.Field\n@Field final String name = 'cas'\nreturn name",
        "enum Color { RED, GREEN }\nreturn Color.RED",
        "class Helper { static final String KEY = 'k' }\nreturn Helper.KEY",
        "return [1, 2].collect { it * 2 }"
    })
    void verifyStateless(final String script) {
        val compiled = ScriptingUtils.compileGroovyShellScript(script);
        assertFalse(compiled.isStateful());
        assertNull(compiled.sharedInstance());
        assertNotSame(compiled.newInstance(true), compiled.newInstance(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "import groovy.transform.Field\n@Field int count = 0\nreturn count++",
        "import groovy.transform.Field\n@Field def logger = org.slf4j.LoggerFactory.getLogger('cas')\nreturn 1",
        "import groovy.transform.Field\n@Field final Map cache = [:]\nreturn cache",
        "import groovy.transform.Field\n@Field static int count = 0\nreturn count++",
        "import groovy.transform.Field\n@Field static final List seen = []\nreturn seen",
        "class Helper { static Map cache = [:] }\nreturn Helper.cache",
        "class Helper { static final Map CACHE = new java.util.concurrent.ConcurrentHashMap() }\nreturn Helper.CACHE"
    })
    void verifyStateful(final String script) {
        val compiled = ScriptingUtils.compileGroovyShellScript(script);
        assertTrue(compiled.isStateful());
        assertNotNull(compiled.sharedInstance());
    }

    @Test
    void verifyScriptFile() throws Throwable {
        val stateless = Files.createTempFile("stateless", ".groovy");
        Files.writeString(stateless, "def run(Object... args) { args[0] }");
        val compiledStateless = ScriptingUtils.compileGroovyScript(new FileSystemResource(stateless), true);
        assertNotNull(compiledStateless);
        assertFalse(compiledStateless.isStateful());

        val stateful = Files.createTempFile("stateful", ".groovy");
        Files.writeString(stateful, "class Counter { int count; def run(Object... args) { count++ } }");
        val compiledStateful = ScriptingUtils.compileGroovyScript(new FileSystemResource(stateful), true);
        assertNotNull(compiledStateful);
        assertTrue(compiledStateful.isStateful());
    }
}
