package org.apereo.cas.util.scripting;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyShellScriptTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
class GroovyShellScriptTests {

    @Test
    void verifyExec() throws Throwable {
        val shell = new GroovyShellScript("println 'test'");
        assertNotNull(shell.getGroovyScript());
        assertNotNull(shell.getScript());

        assertDoesNotThrow(() -> shell.execute(ArrayUtils.EMPTY_OBJECT_ARRAY));
        assertNotNull(shell.toString());
    }

    @Test
    void verifyUnknownBadScript() throws Throwable {
        val shell = new GroovyShellScript("###$$@@@!!!***&&&");
        assertDoesNotThrow(() -> {
            shell.execute(ArrayUtils.EMPTY_OBJECT_ARRAY);
            shell.execute("run", Void.class, ArrayUtils.EMPTY_OBJECT_ARRAY);
        });
    }
}
