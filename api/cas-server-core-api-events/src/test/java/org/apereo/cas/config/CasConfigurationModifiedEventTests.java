package org.apereo.cas.config;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationModifiedEventTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Events")
class CasConfigurationModifiedEventTests {
    @Test
    void verifyOperation() throws Throwable {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val event = new CasConfigurationModifiedEvent(this, File.createTempFile("temp", ".file").toPath(), null);
                assertFalse(event.isEligibleForContextRefresh());
            }
        });
    }
}
