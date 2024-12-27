package org.apereo.cas.support.events.listener;

import org.apereo.cas.config.CasConfigurationModifiedEvent;
import org.apereo.cas.config.CasCoreConfigurationWatchAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreEventsConfigEnvironmentAutoConfiguration;
import org.apereo.cas.config.CasCoreStandaloneBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */

@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreEventsConfigEnvironmentAutoConfiguration.class,
    CasCoreConfigurationWatchAutoConfiguration.class,
    CasCoreStandaloneBootstrapAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class
}, properties = {
    "spring.application.name=cas",
    "spring.profiles.active=standalone",
    "spring.cloud.config.enabled=false"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasConfigurationEventListenerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("casConfigurationEventListener")
    private CasConfigurationEventListener casConfigurationEventListener;

    @Test
    void verifyOperation() {
        assertNotNull(casConfigurationEventListener);
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new EnvironmentChangeEvent(Set.of("cas.server.name"))));
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new CasConfigurationModifiedEvent(this, true, null)));
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new RefreshScopeRefreshedEvent()));
    }
}
