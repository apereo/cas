package org.apereo.cas.web.flow;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DelegatedClientIdentityProviderConfigurationPostProcessorTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
    private DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        assertNotNull(delegatedClientIdentityProviderConfigurationPostProcessor);
        assertDoesNotThrow(() -> {
            val context = MockRequestContext.create(applicationContext);
            delegatedClientIdentityProviderConfigurationPostProcessor.process(context, Set.of());
            delegatedClientIdentityProviderConfigurationPostProcessor.destroy();
        });

    }
}
