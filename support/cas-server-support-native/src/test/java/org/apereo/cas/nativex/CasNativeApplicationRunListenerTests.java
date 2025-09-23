package org.apereo.cas.nativex;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasNativeApplicationRunListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasNativeApplicationRunListenerTests {
    @Test
    void verifyOperation() {
        val factories = SpringFactoriesLoader.loadFactories(SpringApplicationRunListener.class, getClass().getClassLoader());
        assertFalse(factories.isEmpty());
        val listener = factories.stream()
            .filter(CasNativeApplicationRunListener.class::isInstance)
            .findFirst()
            .orElseThrow();
        val environment = new MockEnvironment();
        environment.setActiveProfiles(CasConfigurationPropertiesSourceLocator.PROFILE_NATIVE);
        listener.environmentPrepared(mock(ConfigurableBootstrapContext.class), environment);
        assertTrue(environment.getPropertySources().contains("casNativeCompositeSource"));
    }
}
