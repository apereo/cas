package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.support.events.config.CasConfigurationDeletedEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationDeletedEventTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Events")
class CasConfigurationDeletedEventTests {
    @Test
    void verifyOperation() throws Throwable {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                new CasConfigurationDeletedEvent(this, Files.createTempFile("temp", ".file").toFile().toPath(), null);
            }
        });
    }
}
