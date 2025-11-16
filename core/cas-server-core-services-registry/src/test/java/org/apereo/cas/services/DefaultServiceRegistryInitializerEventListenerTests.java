package org.apereo.cas.services;

import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.util.spring.DirectObjectProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultServiceRegistryInitializerEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
class DefaultServiceRegistryInitializerEventListenerTests {

    @Test
    void verifyOperation() throws Throwable {
        val initializer = mock(ServiceRegistryInitializer.class);
        val listener = new DefaultServiceRegistryInitializerEventListener(new DirectObjectProvider<>(initializer));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                listener.handleConfigurationModifiedEvent(new CasConfigurationModifiedEvent(this, true, null));
            }
        });
        assertDoesNotThrow(() -> listener.handleEnvironmentChangeEvent(new EnvironmentChangeEvent(Set.of())));
        assertDoesNotThrow(() -> listener.handleRefreshScopeRefreshedEvent(new RefreshScopeRefreshedEvent()));
    }
}
