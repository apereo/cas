package org.apereo.cas.support.events.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationCreatedEventTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Events")
public class CasConfigurationCreatedEventTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                new CasConfigurationCreatedEvent(this, File.createTempFile("temp", ".file").toPath());
            }
        });
    }
}
