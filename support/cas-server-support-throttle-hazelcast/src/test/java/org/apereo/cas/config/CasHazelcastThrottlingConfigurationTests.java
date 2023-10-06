package org.apereo.cas.config;

import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasHazelcastThrottlingConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    HazelcastTicketRegistryConfiguration.class,
    HazelcastTicketRegistryTicketCatalogConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasHazelcastThrottlingConfiguration.class
})
@Tag("Hazelcast")
class CasHazelcastThrottlingConfigurationTests {

    @Autowired
    @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
    private ThrottledSubmissionsStore<ThrottledSubmission> throttleSubmissionMap;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(throttleSubmissionMap);
        val submission = ThrottledSubmission.builder().key(UUID.randomUUID().toString()).build();
        throttleSubmissionMap.put(submission);
        assertNotNull(throttleSubmissionMap.get(submission.getKey()));
        assertNotEquals(0, throttleSubmissionMap.entries().count());
        throttleSubmissionMap.removeIf(entry -> entry.getKey().equals(submission.getKey()));
        throttleSubmissionMap.remove(submission.getKey());
        assertEquals(0, throttleSubmissionMap.entries().count());
    }
}
