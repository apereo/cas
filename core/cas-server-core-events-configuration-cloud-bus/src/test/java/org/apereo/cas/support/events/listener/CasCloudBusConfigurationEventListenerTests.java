package org.apereo.cas.support.events.listener;

import org.apereo.cas.config.CasCoreCloudBusEventsConfigEnvironmentAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
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
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCloudBusConfigurationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreCloudBusEventsConfigEnvironmentAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasCloudBusConfigurationEventListenerTests {
    @Autowired
    @Qualifier("casCloudBusConfigurationEventListener")
    private DefaultCasCloudBusConfigurationEventListener casCloudBusConfigurationEventListener;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        assertNotNull(casCloudBusConfigurationEventListener);
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new RefreshRemoteApplicationEvent(this, "service", "destination")));
    }

}
