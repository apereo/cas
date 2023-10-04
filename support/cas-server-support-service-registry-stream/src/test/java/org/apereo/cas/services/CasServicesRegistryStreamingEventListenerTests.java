package org.apereo.cas.services;

import org.apereo.cas.config.CasServicesStreamingConfiguration;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServicesRegistryStreamingEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasServicesStreamingConfiguration.class
}, properties = "cas.service-registry.stream.core.enabled=true")
@Tag("RegisteredService")
class CasServicesRegistryStreamingEventListenerTests {
    @Autowired
    @Qualifier("casServicesRegistryStreamingEventListener")
    private CasServicesRegistryStreamingEventListener casServicesRegistryStreamingEventListener;

    @Test
    void verifyDeleted() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val clientInfo = ClientInfoHolder.getClientInfo();
        assertDoesNotThrow(() -> casServicesRegistryStreamingEventListener.handleCasRegisteredServiceDeletedEvent(
            new CasRegisteredServiceDeletedEvent(this, service, clientInfo)));
    }

    @Test
    void verifyLoaded() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val clientInfo = ClientInfoHolder.getClientInfo();
        assertDoesNotThrow(() -> casServicesRegistryStreamingEventListener.handleCasRegisteredServiceLoadedEvent(
            new CasRegisteredServiceLoadedEvent(this, service, clientInfo)));
    }

    @Test
    void verifySaved() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val clientInfo = ClientInfoHolder.getClientInfo();
        assertDoesNotThrow(() -> casServicesRegistryStreamingEventListener.handleCasRegisteredServiceSavedEvent(
            new CasRegisteredServiceSavedEvent(this, service, clientInfo)));
    }
}
