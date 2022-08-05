package org.apereo.cas.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.extractor.ValueCollector;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
        HazelcastSessionConfiguration.class
    },
    properties = "cas.webflow.session.hazelcast.cluster.core.instance-name=hzsessioninstance")
@Tag("Hazelcast")
public class HazelcastSessionConfigurationTests {
    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    @Test
    public void verifyOperation() {
        assertNotNull(hazelcastInstance);
        val extractor = new HazelcastSessionPrincipalNameExtractor();
        assertDoesNotThrow(() -> extractor.extract(new MapSession(), "casuser", mock(ValueCollector.class)));
    }

    @AfterEach
    public void shutdown() {
        hazelcastInstance.shutdown();
    }
}
