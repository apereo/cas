package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentJdbcConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    CasConsentJdbcConfiguration.class,
    CasHibernateJpaConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.jdbc.show-sql=false",
    "cas.consent.jpa.ddl-auto=create-drop"
})
@Getter
@Tag("JDBC")
public class JpaConsentRepositoryTests extends BaseConsentRepositoryTests {
    private static final int COUNT = 1000;

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;

    @Test
    public void verifyBadDelete() {
        assertFalse(repository.deleteConsentDecision(-1, null));
    }

    @Test
    public void verifyLargeDataset() {
        val principal = UUID.randomUUID().toString();
        for (int i = 0; i < COUNT; i++) {
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
            val decision = BUILDER.build(service, registeredService, principal,
                CollectionUtils.wrap("attribute" + i, List.of("value" + i)));
            repository.storeConsentDecision(decision);
        }

        var stopwatch = new StopWatch();
        stopwatch.start();
        assertEquals(COUNT, repository.findConsentDecisions(principal).size());
        stopwatch.stop();
        assertTrue(stopwatch.getTime(TimeUnit.SECONDS) <= 10);
    }
}
