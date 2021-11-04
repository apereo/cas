package org.apereo.cas.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.extractor.ValueCollector;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
        val extractor = new HazelcastSessionConfiguration.HazelcastSessionPrincipalNameExtractor();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                extractor.extract(new MapSession(), "casuser", mock(ValueCollector.class));
            }
        });
    }
}
