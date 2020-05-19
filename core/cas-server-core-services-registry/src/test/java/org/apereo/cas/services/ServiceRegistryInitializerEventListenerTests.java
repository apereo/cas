package org.apereo.cas.services;

import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceRegistryInitializerEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class ServiceRegistryInitializerEventListenerTests {

    @Test
    public void verifyOperation() {
        val listener = new ServiceRegistryInitializerEventListener(mock(ServiceRegistryInitializer.class));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                listener.handleConfigurationModifiedEvent(new CasConfigurationModifiedEvent(this, true));
            }
        });
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                listener.handleRefreshEvent(new EnvironmentChangeEvent(Set.of()));
            }
        });
    }
}
