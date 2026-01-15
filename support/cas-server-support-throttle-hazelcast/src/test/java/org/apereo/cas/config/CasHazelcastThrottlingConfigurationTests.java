package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasHazelcastThrottlingConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
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
},
    properties = {
        "cas.authn.throttle.hazelcast.cluster.network.port-auto-increment=false",
        "cas.authn.throttle.hazelcast.cluster.network.port=5710",
        "cas.authn.throttle.hazelcast.cluster.core.instance-name=throttlehzstore"
    })
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class CasHazelcastThrottlingConfigurationTests {

    @Autowired
    @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
    private ThrottledSubmissionsStore<ThrottledSubmission> throttleSubmissionStore;

    @Test
    void verifyOperation() {
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
