package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJpaServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.hibernate.Session;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.support.TransactionOperations;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author battags
 * @since 3.1.0
 */
@SpringBootTest(classes = {
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    CasJpaServiceRegistryAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class
},
    properties = "cas.jdbc.show-sql=false")
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
@Getter
class JpaServiceRegistryTests extends AbstractServiceRegistryTests {
    private static final int COUNT = 10_000;

    @Autowired
    @Qualifier("jpaServiceRegistry")
    protected ServiceRegistry newServiceRegistry;

    @Autowired
    @Qualifier("serviceEntityManagerFactory")
    protected EntityManagerFactory serviceEntityManagerFactory;

    @Autowired
    @Qualifier("jdbcServiceRegistryTransactionTemplate")
    protected TransactionOperations jdbcServiceRegistryTransactionTemplate;

    @Autowired
    @Qualifier("dataSourceService")
    protected DataSource dataSourceService;

    private long countManagedEntities() {
        val entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(serviceEntityManagerFactory);
        return Objects.requireNonNull(entityManager).unwrap(Session.class).getStatistics().getEntityCount();
    }

    private RegisteredService findLoadedService(final long id) {
        return newServiceRegistry.load().stream().filter(service -> service.getId() == id).findFirst().orElseThrow();
    }

    @Test
    void verifyUnchangedServicesAreReused() {
        val saved = newServiceRegistry.save(buildRegisteredServiceInstance(RandomUtils.nextInt(), CasRegisteredService.class)
            .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE));
        val loaded = findLoadedService(saved.getId());
        assertSame(loaded, findLoadedService(saved.getId()));
        assertSame(loaded, newServiceRegistry.getServicesStream()
            .filter(service -> service.getId() == saved.getId()).findFirst().orElseThrow());

        saved.setEvaluationOrder(loaded.getEvaluationOrder() + 1);
        newServiceRegistry.save(saved);
        val updated = findLoadedService(saved.getId());
        assertNotSame(loaded, updated);
        assertEquals(saved.getEvaluationOrder(), updated.getEvaluationOrder());

        assertTrue(newServiceRegistry.delete(updated));
        assertTrue(newServiceRegistry.load().stream().noneMatch(service -> service.getId() == saved.getId()));
    }

    @Test
    void verifyReadsKeepNoEntities() {
        val saved = newServiceRegistry.save(buildRegisteredServiceInstance(RandomUtils.nextInt(), CasRegisteredService.class)
            .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE));
        val managedEntities = jdbcServiceRegistryTransactionTemplate.execute(_ -> {
            assertFalse(newServiceRegistry.load().isEmpty());
            assertEquals(1, newServiceRegistry.getServicesStream().filter(service -> service.getId() == saved.getId()).count());
            assertNotNull(newServiceRegistry.findServiceByExactServiceName(saved.getName()));
            assertNotNull(newServiceRegistry.findServiceByExactServiceId(saved.getServiceId()));
            val found = Objects.requireNonNull(newServiceRegistry.findServiceById(saved.getId()));
            val count = countManagedEntities();
            found.setEvaluationOrder(saved.getEvaluationOrder() + 10);
            newServiceRegistry.save(found);
            return count;
        });
        assertEquals(0L, managedEntities);
        assertEquals(saved.getEvaluationOrder() + 10,
            Objects.requireNonNull(newServiceRegistry.findServiceById(saved.getId())).getEvaluationOrder());
    }

    @Test
    void verifyDeleteNeedsOnlyIdentifier() {
        val saved = newServiceRegistry.save(buildRegisteredServiceInstance(RandomUtils.nextInt(), CasRegisteredService.class)
            .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE));
        val reference = new CasRegisteredService();
        reference.setId(saved.getId());
        assertTrue(newServiceRegistry.delete(reference));
        assertNull(newServiceRegistry.findServiceById(saved.getId()));
    }

    @Test
    void verifyLookupColumnsAreIndexed() throws Exception {
        try (val connection = dataSourceService.getConnection()) {
            val metadata = connection.getMetaData();
            val indexes = new HashSet<String>();
            for (val table : List.of("registered_services", "REGISTERED_SERVICES")) {
                try (val results = metadata.getIndexInfo(connection.getCatalog(), null, table, false, true)) {
                    while (results.next()) {
                        Optional.ofNullable(results.getString("INDEX_NAME")).map(name -> name.toLowerCase(Locale.ROOT)).ifPresent(indexes::add);
                    }
                }
            }
            assertTrue(indexes.containsAll(List.of("idx_regsvc_service_id", "idx_regsvc_name")), indexes::toString);
        }
    }

    @Test
    void verifyLargeDataset() {
        val maximumManagedEntities = new AtomicLong();
        newServiceRegistry.save(
            () -> {
                val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true)
                    .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
                svc.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
                return svc;
            },
            result -> maximumManagedEntities.accumulateAndGet(countManagedEntities(), Math::max),
            COUNT);
        assertTrue(maximumManagedEntities.get() <= 100, () -> "Persistence context grew to " + maximumManagedEntities.get());
        var stopwatch = new StopWatch();
        stopwatch.start();
        assertEquals(newServiceRegistry.size(), newServiceRegistry.load().size());
        stopwatch.stop();
        assertTrue(stopwatch.getTime(TimeUnit.SECONDS) <= 10);
    }

    @Test
    void verifySaveInStreams() {
        var servicesToImport = Stream.<RegisteredService>empty();
        for (var i = 0; i < 1000; i++) {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            registeredService.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            servicesToImport = Stream.concat(servicesToImport, Stream.of(registeredService));
        }
        var stopwatch = new StopWatch();
        newServiceRegistry.save(servicesToImport);
        stopwatch.start();
        assertEquals(newServiceRegistry.size(), newServiceRegistry.load().size());
        stopwatch.stop();
        assertTrue(stopwatch.getTime(TimeUnit.SECONDS) <= 10);
    }

    @Test
    void verifyEntityAttachment() {
        var service = buildRegisteredServiceInstance(RandomUtils.nextInt(), CasRegisteredService.class).assignIdIfNecessary();
        service = newServiceRegistry.save(service);
        assertNotNull(service);
        service = newServiceRegistry.findServiceById(service.getId(), CasRegisteredService.class);
        assertNotNull(service);
        service.setEvaluationOrder(999);
        val currentId = service.getId();
        service = newServiceRegistry.save(service);
        assertEquals(currentId, service.getId());
        service = newServiceRegistry.findServiceById(service.getId(), CasRegisteredService.class);
        assertEquals(999, service.getEvaluationOrder());
    }
}
