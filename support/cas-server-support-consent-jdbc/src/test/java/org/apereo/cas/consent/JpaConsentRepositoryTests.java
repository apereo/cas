package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.config.CasConsentJdbcAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    CasConsentJdbcAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.jdbc.show-sql=false",
    "cas.consent.jpa.ddl-auto=create-drop"
})
@Getter
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
class JpaConsentRepositoryTests extends BaseConsentRepositoryTests {
    private static final int COUNT = 1000;

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    protected ConsentRepository repository;

    @Test
    void verifyBadDelete() throws Throwable {
        assertFalse(repository.deleteConsentDecision(-1, null));
    }

    @Test
    void verifyLargeDataset() throws Throwable {
        val principal = UUID.randomUUID().toString();
        for (var i = 0; i < COUNT; i++) {
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
            val decision = BUILDER.build(service, registeredService, principal,
                CollectionUtils.wrap("attribute%d".formatted(i), List.of("value%d".formatted(i))));
            repository.storeConsentDecision(decision);
        }

        var stopwatch = new StopWatch();
        stopwatch.start();
        assertEquals(COUNT, repository.findConsentDecisions(principal).size());
        stopwatch.stop();
        assertTrue(stopwatch.getTime(TimeUnit.SECONDS) <= 10);
    }
}
