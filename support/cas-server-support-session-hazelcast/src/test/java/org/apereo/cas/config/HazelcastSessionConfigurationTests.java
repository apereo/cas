package org.apereo.cas.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.extractor.ValueCollector;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.session.MapSession;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastSessionConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(
    classes = {
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        HazelcastSessionConfiguration.class
    },
    properties = {
        "cas.webflow.session.server.hazelcast.cluster.network.port-auto-increment=false",
        "cas.webflow.session.server.hazelcast.cluster.network.port=5709",
        "cas.webflow.session.server.hazelcast.cluster.core.instance-name=hzsessioninstance"
    })
@Tag("Hazelcast")
@Execution(ExecutionMode.SAME_THREAD)
class HazelcastSessionConfigurationTests {
    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(hazelcastInstance);
        val extractor = new HazelcastSessionPrincipalNameExtractor();
        assertDoesNotThrow(() -> extractor.extract(new MapSession(), "casuser", mock(ValueCollector.class)));
    }

    @AfterEach
    public void shutdown() {
        hazelcastInstance.shutdown();
    }
}
