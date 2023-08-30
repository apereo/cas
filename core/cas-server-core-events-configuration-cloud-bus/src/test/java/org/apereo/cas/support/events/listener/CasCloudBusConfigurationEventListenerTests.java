package org.apereo.cas.support.events.listener;

import org.apereo.cas.config.CasCloudBusEventsConfigEnvironmentConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCloudBusConfigurationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    CasCloudBusEventsConfigEnvironmentConfiguration.class,
    CasCoreEnvironmentBootstrapConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
class CasCloudBusConfigurationEventListenerTests {
    @Autowired
    @Qualifier("casCloudBusConfigurationEventListener")
    private DefaultCasCloudBusConfigurationEventListener casCloudBusConfigurationEventListener;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(casCloudBusConfigurationEventListener);
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new RefreshRemoteApplicationEvent(this, "service", "destination")));
    }

}
