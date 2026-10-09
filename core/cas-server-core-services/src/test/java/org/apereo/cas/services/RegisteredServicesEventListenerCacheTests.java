package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServicesEventListenerCacheTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
class RegisteredServicesEventListenerCacheTests {

    private static CasRegisteredService newRegisteredService() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId("https://%s.example.org/app".formatted(registeredService.getName()));
        return registeredService;
    }

    @Nested
    class HandlerTests {
        private final CacheableServicesManager servicesManager = mock(CacheableServicesManager.class);

        private final RegisteredServicesEventListener listener = newListener(servicesManager);

        @Test
        void verifyServiceSavedOutsideServicesManagerIsCached() {
            val registeredService = newRegisteredService();
            listener.handleRegisteredServiceSavedEvent(new CasRegisteredServiceSavedEvent(this, registeredService, ClientInfoHolder.getClientInfo()));
            verify(servicesManager).cacheRegisteredService(registeredService);
        }

        @Test
        void verifyServiceSavedByServicesManagerIsIgnored() {
            val registeredService = newRegisteredService();
            listener.handleRegisteredServiceSavedEvent(
                new CasRegisteredServiceSavedEvent(mock(ServicesManager.class), registeredService, ClientInfoHolder.getClientInfo()));
            verify(servicesManager, never()).cacheRegisteredService(any());
        }

        @Test
        void verifyServiceDeletedOutsideServicesManagerIsEvicted() {
            val registeredService = newRegisteredService();
            listener.handleRegisteredServiceDeletedEvent(new CasRegisteredServiceDeletedEvent(this, registeredService, ClientInfoHolder.getClientInfo()));
            verify(servicesManager).removeRegisteredServiceFromCache(registeredService);
        }

        @Test
        void verifyServiceDeletedByServicesManagerIsIgnored() {
            listener.handleRegisteredServiceDeletedEvent(
                new CasRegisteredServiceDeletedEvent(mock(ServicesManager.class), newRegisteredService(), ClientInfoHolder.getClientInfo()));
            verify(servicesManager, never()).removeRegisteredServiceFromCache(any());
        }

        @Test
        void verifyRegistryReloadOutsideServicesManagerReloadsServices() {
            listener.handleRegisteredServicesLoadedEvent(new CasRegisteredServicesLoadedEvent(this, List.of(), ClientInfoHolder.getClientInfo()));
            verify(servicesManager).load();
        }

        @Test
        void verifyServicesManagerReloadIsIgnored() {
            listener.handleRegisteredServicesLoadedEvent(
                new CasRegisteredServicesLoadedEvent(mock(ServicesManager.class), List.of(), ClientInfoHolder.getClientInfo()));
            verify(servicesManager, never()).load();
        }

        @Test
        void verifyServicesManagerWithoutCache() {
            val uncachedListener = newListener(mock(ServicesManager.class));
            val registeredService = newRegisteredService();
            assertDoesNotThrow(() -> {
                uncachedListener.handleRegisteredServiceSavedEvent(
                    new CasRegisteredServiceSavedEvent(this, registeredService, ClientInfoHolder.getClientInfo()));
                uncachedListener.handleRegisteredServiceDeletedEvent(
                    new CasRegisteredServiceDeletedEvent(this, registeredService, ClientInfoHolder.getClientInfo()));
            });
        }

        private static RegisteredServicesEventListener newListener(final ServicesManager servicesManager) {
            return new DefaultRegisteredServicesEventListener(servicesManager, new CasConfigurationProperties(),
                mock(CommunicationsManager.class), mock(TenantExtractor.class));
        }
    }

    @Nested
    @SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.service-registry.cache.initial-capacity=100")
    class ApplicationEventTests {
        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        private ServicesManager servicesManager;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyPublishedRegistryEventsUpdateServicesCache() {
            assertInstanceOf(CacheableServicesManager.class, servicesManager);
            servicesManager.load();

            val registeredService = newRegisteredService();
            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            assertNull(servicesManager.findServiceBy(service));

            applicationContext.publishEvent(new CasRegisteredServiceSavedEvent(this, registeredService, ClientInfoHolder.getClientInfo()));
            assertTrue(((CacheableServicesManager) servicesManager).getCachedRegisteredServices().containsKey(registeredService.getId()));
            assertNotNull(servicesManager.findServiceBy(service));

            applicationContext.publishEvent(new CasRegisteredServiceDeletedEvent(this, registeredService, ClientInfoHolder.getClientInfo()));
            assertFalse(((CacheableServicesManager) servicesManager).getCachedRegisteredServices().containsKey(registeredService.getId()));
        }
    }
}
