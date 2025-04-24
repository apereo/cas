package org.apereo.cas.services;

import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author battags
 * @since 3.0.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class DefaultServicesManagerTests {

    @Nested
    @SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.service-registry.core.index-services=false")
    class NoIndexingTests extends AbstractServicesManagerTests {
        @Test
        void verifyQuerying() throws Exception {
            val registeredService = new CasRegisteredService();
            registeredService.setId(RandomUtils.nextLong());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(registeredService.getName());
            servicesManager.save(registeredService);

            val query1 = RegisteredServiceQuery.of(CasRegisteredService.class, "id", registeredService.getId());
            var foundService = servicesManager.findServicesBy(query1).findFirst().orElseThrow();
            assertEquals(foundService, registeredService);

            val query2 = RegisteredServiceQuery.of(CasRegisteredService.class, "name", registeredService.getName(), true);
            foundService = servicesManager.findServicesBy(query2).findFirst().orElseThrow();
            assertEquals(foundService, registeredService);

            val query3 = RegisteredServiceQuery.of(CasRegisteredService.class, "unknownProp", registeredService.getName(), true);
            assertTrue(servicesManager.findServicesBy(query3).findFirst().isEmpty());

            val query4 = RegisteredServiceQuery.of(CasRegisteredService.class, "name", "unknown-name", true);
            assertTrue(servicesManager.findServicesBy(query4).findFirst().isEmpty());
        }
    }

    @Nested
    @SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
    class DefaultTests extends AbstractServicesManagerTests {
        @Test
        void verifyOperation() {
            val mock = mock(ServicesManager.class);
            when(mock.findServiceBy(anyLong(), any())).thenCallRealMethod();
            when(mock.findServiceByName(anyString(), any())).thenCallRealMethod();
            when(mock.count()).thenCallRealMethod();
            when(mock.getName()).thenCallRealMethod();
            when(mock.getOrder()).thenCallRealMethod();
            assertEquals(Ordered.LOWEST_PRECEDENCE, mock.getOrder());
            assertEquals(0, mock.count());
            assertNotNull(mock.getName());
            assertNull(mock.findServiceBy(0, CasRegisteredService.class));
        }

        @Test
        void verifySupports() {
            val registeredService = new CasRegisteredService();
            registeredService.setId(RandomUtils.nextLong());
            registeredService.setName("domainService1");
            registeredService.setServiceId("https://www.example.com/one");
            servicesManager.save(registeredService);
            assertTrue(servicesManager.supports(CasRegisteredService.class));
            assertTrue(servicesManager.supports(registeredService));
            assertTrue(servicesManager.supports(RegisteredServiceTestUtils.getService()));
        }

        @Test
        void verifySaveWithDomains() {
            val svc = new CasRegisteredService();
            svc.setId(RandomUtils.nextLong());
            svc.setName("domainService2");
            svc.setServiceId("https://www.example.com/" + svc.getId());
            assertNotNull(servicesManager.save(svc, false));
            assertEquals(1, servicesManager.getDomains().count());
            assertFalse(servicesManager.getServicesForDomain("example.org").isEmpty());
        }

        @Test
        void verifySaveInBulk() {
            servicesManager.deleteAll();
            servicesManager.save(() -> {
                val svc = new CasRegisteredService();
                svc.setId(RandomUtils.nextLong());
                svc.setName("domainService2");
                svc.setServiceId("https://www.example.com/" + svc.getId());
                return svc;
            }, Assertions::assertNotNull, 10);
            val results = servicesManager.load();
            assertEquals(10, results.size());
        }

        @Test
        void verifySaveInStreams() {
            servicesManager.deleteAll();
            val s1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
            val s2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
            servicesManager.save(Stream.of(s1, s2));
            val results = servicesManager.load();
            assertEquals(2, results.size());
        }

        @Test
        void verifyFindByQuery() {
            val service = new CasRegisteredService();
            service.setId(RandomUtils.nextLong());
            service.setName("%s%d".formatted(UUID.randomUUID().toString(), service.getId()));
            service.setServiceId(service.getName());
            servicesManager.save(service);
            assertEquals(0, servicesManager.findServicesBy().count());
            assertEquals(1, servicesManager.findServicesBy(
                RegisteredServiceQuery.of(CasRegisteredService.class, "id", service.getId())).count());
            assertEquals(1, servicesManager.findServicesBy(
                RegisteredServiceQuery.of(CasRegisteredService.class, "id", service.getId()),
                RegisteredServiceQuery.of(CasRegisteredService.class, "name", service.getName())).count());
        }

        @Test
        void verifyFindByName() {
            val registeredService = new CasRegisteredService();
            registeredService.setId(RandomUtils.nextLong());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(registeredService.getName());

            assertNull(servicesManager.findServiceByName(null));

            serviceRegistry.save(registeredService);
            assertNotNull(servicesManager.findServiceByName(registeredService.getName()));
            assertNotEquals(0, servicesManager.stream().count());
            assertEquals(1, servicesManager.getDomains().count());
            assertFalse(servicesManager.getServicesForDomain(UUID.randomUUID().toString()).isEmpty());
        }

        @Test
        void verifyFindByNameAndType() {
            val registeredService = new CasRegisteredService();
            registeredService.setId(RandomUtils.nextLong());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(registeredService.getName());
            serviceRegistry.save(registeredService);
            assertNotNull(servicesManager.findServiceByName(registeredService.getName(), CasRegisteredService.class));
            assertNotEquals(0, servicesManager.stream().count());
        }

        @Test
        void verifySaveAndRemoveFromCache() throws InterruptedException {
            val registeredService = new CasRegisteredService();
            registeredService.setId(RandomUtils.nextLong());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(registeredService.getName());
            assertFalse(isServiceInCache(null, registeredService.getId()));
            this.servicesManager.save(registeredService);
            assertTrue(isServiceInCache(null, registeredService.getId()));
            Thread.sleep(1_000);
            assertTrue(isServiceInCache(null, registeredService.getId()));
            Thread.sleep(5_000);
            assertTrue(isServiceInCache(null, registeredService.getId()));
        }

        @Test
        void verifyEmptyCacheFirst() {
            val registeredService = new CasRegisteredService();
            registeredService.setId(RandomUtils.nextLong());
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(registeredService.getName());
            assertFalse(isServiceInCache(null, registeredService.getId()));
            servicesManager.save(registeredService);
            assertTrue(isServiceInCache(null, registeredService.getId()));
            servicesManager.load();
            assertTrue(isServiceInCache(null, registeredService.getId()));
        }
    }

    @Nested
    @SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
    class IndexableTests {
        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        private ChainingServicesManager servicesManager;
        
        @Test
        void verifyOperation() {
            val indexedServicesManager = (IndexableServicesManager) servicesManager.getServiceManagers().getFirst();
            indexedServicesManager.clearIndexedServices();
            assertEquals(0, indexedServicesManager.countIndexedServices());

            var registeredService = getRegisteredService(RandomUtils.nextLong());
            indexedServicesManager.save(registeredService);

            registeredService = getRegisteredService(registeredService.getId());
            registeredService.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of("one", "two")));
            registeredService.setDescription(UUID.randomUUID().toString());
            indexedServicesManager.save(registeredService);

            assertEquals(1, indexedServicesManager.countIndexedServices());
            assertTrue(indexedServicesManager.findIndexedServiceBy(registeredService.getId()).isPresent());
        }

        private static CasRegisteredService getRegisteredService(final long id) {
            val registeredService = new CasRegisteredService();
            registeredService.setId(id);
            registeredService.setName(UUID.randomUUID().toString());
            registeredService.setServiceId(registeredService.getName());
            return registeredService;
        }
    }

    @Nested
    @SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.service-registry.cache.cache-size=0")
    class NoCacheTests extends AbstractServicesManagerTests {
    }
}
