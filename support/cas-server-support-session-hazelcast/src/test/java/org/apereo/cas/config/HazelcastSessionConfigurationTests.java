package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.hazelcast.core.HazelcastInstance;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.SessionRepository;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastSessionConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasHazelcastSessionAutoConfiguration.class, properties = {
    "cas.webflow.session.server.hazelcast.cluster.network.port-auto-increment=false",
    "cas.webflow.session.server.hazelcast.cluster.network.port=5709",
    "cas.webflow.session.server.hazelcast.cluster.core.instance-name=hzsessioninstance"
})
@Tag("Hazelcast")
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(CasTestExtension.class)
class HazelcastSessionConfigurationTests {
    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    @Autowired
    @Qualifier("sessionRepository")
    private SessionRepository sessionRepository;

    @Test
    void verifyOperation() {
        assertNotNull(hazelcastInstance);
        assertNotNull(sessionRepository);
        val session = sessionRepository.createSession();
        assertNotNull(session);
        sessionRepository.save(session);
        val result = sessionRepository.findById(session.getId());
        assertNotNull(result);
        sessionRepository.deleteById(session.getId());
        val deleted = sessionRepository.findById(session.getId());
        assertNull(deleted);
    }

    @AfterEach
    public void shutdown() {
        hazelcastInstance.shutdown();
    }
}
