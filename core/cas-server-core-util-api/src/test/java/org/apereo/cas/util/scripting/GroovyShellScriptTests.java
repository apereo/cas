package org.apereo.cas.util.scripting;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyShellScriptTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
public class GroovyShellScriptTests {

    @Test
    public void verifyExec() {
        val shell = new GroovyShellScript("println 'test'");
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                shell.execute(ArrayUtils.EMPTY_OBJECT_ARRAY);
            }
        });
        assertNotNull(shell.toString());
    }
}
