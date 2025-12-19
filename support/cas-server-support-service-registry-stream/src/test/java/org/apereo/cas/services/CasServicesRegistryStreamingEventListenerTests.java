package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasServicesStreamingAutoConfiguration;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServicesRegistryStreamingEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasServicesStreamingAutoConfiguration.class, properties = "cas.service-registry.stream.core.enabled=true")
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
class CasServicesRegistryStreamingEventListenerTests {
    @Autowired
    @Qualifier("casServicesRegistryStreamingEventListener")
    private CasServicesRegistryStreamingEventListener casServicesRegistryStreamingEventListener;

    @Test
    void verifyDeleted() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val clientInfo = ClientInfoHolder.getClientInfo();
        assertDoesNotThrow(() -> casServicesRegistryStreamingEventListener.handleCasRegisteredServiceDeletedEvent(
            new CasRegisteredServiceDeletedEvent(this, service, clientInfo)));
    }

    @Test
    void verifyLoaded() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val clientInfo = ClientInfoHolder.getClientInfo();
        assertDoesNotThrow(() -> casServicesRegistryStreamingEventListener.handleCasRegisteredServiceLoadedEvent(
            new CasRegisteredServiceLoadedEvent(this, service, clientInfo)));
    }

    @Test
    void verifySaved() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val clientInfo = ClientInfoHolder.getClientInfo();
        assertDoesNotThrow(() -> casServicesRegistryStreamingEventListener.handleCasRegisteredServiceSavedEvent(
            new CasRegisteredServiceSavedEvent(this, service, clientInfo)));
    }
}
