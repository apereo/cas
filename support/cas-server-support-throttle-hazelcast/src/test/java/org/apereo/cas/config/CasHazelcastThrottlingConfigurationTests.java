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
    CasHazelcastTicketRegistryAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasHazelcastThrottlingAutoConfiguration.class
})
@Tag("Hazelcast")
class CasHazelcastThrottlingConfigurationTests {

    @Autowired
    @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
    private ThrottledSubmissionsStore<ThrottledSubmission> throttleSubmissionStore;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(throttleSubmissionStore);
        val submission = ThrottledSubmission.builder().key(UUID.randomUUID().toString()).build();
        throttleSubmissionStore.put(submission);
        assertNotNull(throttleSubmissionStore.get(submission.getKey()));
        assertNotEquals(0, throttleSubmissionStore.entries().count());
        throttleSubmissionStore.removeIf(entry -> entry.getKey().equals(submission.getKey()));
        throttleSubmissionStore.remove(submission.getKey());
        assertEquals(0, throttleSubmissionStore.entries().count());
    }
}
