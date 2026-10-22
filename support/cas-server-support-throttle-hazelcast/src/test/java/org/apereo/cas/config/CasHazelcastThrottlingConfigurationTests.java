package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.support.HazelcastMapThrottledSubmissionsStore;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.awaitility.Awaitility.*;
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
        "cas.authn.throttle.hazelcast.cluster.network.port=5710",
        "cas.authn.throttle.hazelcast.cluster.network.tcpip-enabled=false",
        "cas.ticket.registry.hazelcast.cluster.network.tcpip-enabled=false",
        "cas.authn.throttle.hazelcast.cluster.core.instance-name=throttlehzstore"
    })
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class CasHazelcastThrottlingConfigurationTests {

    @Autowired
    @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
    private ThrottledSubmissionsStore<ThrottledSubmission> throttleSubmissionStore;

    @Autowired
    private CasConfigurationProperties casProperties;

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

    @Test
    void verifyRemovalWithEntriesOnOtherMembers() {
        val clusterName = UUID.randomUUID().toString();
        val first = Hazelcast.newHazelcastInstance(newMemberConfig(clusterName, null));
        val address = first.getCluster().getLocalMember().getAddress();
        val second = Hazelcast.newHazelcastInstance(newMemberConfig(clusterName, address.getHost() + ':' + address.getPort()));
        try {
            await().atMost(Duration.ofSeconds(30)).until(() -> first.getCluster().getMembers().size() == 2);
            val store = new HazelcastMapThrottledSubmissionsStore<ThrottledSubmission>(first.getMap("ipMap"), casProperties);
            val submissions = IntStream.range(0, 50)
                .<ThrottledSubmission>mapToObj(_ -> ThrottledSubmission.builder().key(UUID.randomUUID().toString()).build())
                .toList();
            submissions.forEach(store::put);
            val removed = submissions.getFirst().getKey();
            store.removeIf(entry -> entry.getKey().equals(removed));
            assertNull(store.get(removed));
            assertEquals(49, store.entries().count());
        } finally {
            second.shutdown();
            first.shutdown();
        }
    }

    private static Config newMemberConfig(final String clusterName, final String member) {
        val config = new Config().setClusterName(clusterName).setInstanceName(clusterName + '-' + UUID.randomUUID());
        config.getNetworkConfig().setPort(5730).setPortAutoIncrement(true);
        val join = config.getNetworkConfig().getJoin();
        join.getAutoDetectionConfig().setEnabled(false);
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true).addMember(Objects.requireNonNullElse(member, "127.0.0.1"));
        return config;
    }
}
