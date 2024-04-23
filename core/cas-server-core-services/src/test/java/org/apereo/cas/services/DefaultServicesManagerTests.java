package org.apereo.cas.services;

import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author battags
 * @since 3.0.0
 */
@Tag("RegisteredService")
@Execution(ExecutionMode.SAME_THREAD)
class DefaultServicesManagerTests {

    @Nested
    @SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.service-registry.core.index-services=false")
    class NoIndexingTests extends AbstractServicesManagerTests {
    }

    @Nested
    @SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
    class DefaultTests extends AbstractServicesManagerTests {
        @Test
        void verifyOperation() throws Throwable {
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
        void verifySupports() throws Throwable {
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
        void verifySaveWithDomains() throws Throwable {
            val svc = new CasRegisteredService();
            svc.setId(RandomUtils.nextLong());
            svc.setName("domainService2");
            svc.setServiceId("https://www.example.com/" + svc.getId());
            assertNotNull(servicesManager.save(svc, false));
            assertEquals(1, servicesManager.getDomains().count());
            assertFalse(servicesManager.getServicesForDomain("example.org").isEmpty());
        }

        @Test
        void verifySaveInBulk() throws Throwable {
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
        void verifySaveInStreams() throws Throwable {
            servicesManager.deleteAll();
            val s1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
            val s2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
            servicesManager.save(Stream.of(s1, s2));
            val results = servicesManager.load();
            assertEquals(2, results.size());
        }

        @Test
        void verifyFindByQuery() throws Throwable {
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
        void verifyFindByName() throws Throwable {
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
        void verifyFindByNameAndType() throws Throwable {
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
        void verifyEmptyCacheFirst() throws Throwable {
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

}
