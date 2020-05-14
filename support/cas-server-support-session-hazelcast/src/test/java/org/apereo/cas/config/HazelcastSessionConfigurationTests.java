package org.apereo.cas.config;

import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastSessionConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    HazelcastSessionConfiguration.class
})
@Tag("Hazelcast")
public class HazelcastSessionConfigurationTests {
    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    @Test
    public void verifyOperation() {
        assertNotNull(hazelcastInstance);
    }
}
