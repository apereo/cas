package org.apereo.cas.logging;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Log4jInitializationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Simple")
public class Log4jInitializationTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(() -> {
            val init = new Log4jInitialization();
            init.setMainArguments(new String[]{"--logging.level.org.apereo.cas=debug"});
        });
    }
}
