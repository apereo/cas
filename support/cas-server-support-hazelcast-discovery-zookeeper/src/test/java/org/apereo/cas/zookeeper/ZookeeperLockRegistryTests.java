package org.apereo.cas.zookeeper;

import org.apereo.cas.config.CasHazelcastZooKeeperAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.lock.LockRepository;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ZookeeperLockRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("ZooKeeper")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 2181)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasHazelcastZooKeeperAutoConfiguration.class, properties = {
    "cas.ticket.registry.hazelcast.cluster.discovery.zookeeper.url=localhost:2181",
    "cas.ticket.registry.core.enable-locking=true"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ZookeeperLockRegistryTests {

    @Autowired
    @Qualifier("casTicketRegistryLockRepository")
    private LockRepository casTicketRegistryLockRepository;

    @Test
    void verifyRepository() {
        val lockKey = UUID.randomUUID().toString();

        val container = new Container();
        container.values.put(lockKey, new ArrayList<>());

        val threads = new ArrayList<Thread>();
        IntStream.range(0, 10).forEach(i -> {
            val thread = new Thread(Unchecked.runnable(() -> {
                Thread.sleep(250);
                casTicketRegistryLockRepository.execute(lockKey, () -> {
                    container.values.get(lockKey).add(UUID.randomUUID().toString());
                    return null;
                });
            }));
            thread.setName("Thread-" + i);
            threads.add(thread);
            thread.start();
        });
        for (val thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                fail(e);
            }
        }
        assertEquals(10, container.values.get(lockKey).size());
    }

    private static final class Container {
        private final Map<String, List<String>> values = new HashMap<>();
    }
}
