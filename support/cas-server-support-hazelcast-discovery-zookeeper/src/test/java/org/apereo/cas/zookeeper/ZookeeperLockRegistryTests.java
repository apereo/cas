package org.apereo.cas.zookeeper;

import org.apereo.cas.config.HazelcastZooKeeperConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.util.lock.LockRepository;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
@EnabledIfPortOpen(port = 2181)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    HazelcastZooKeeperConfiguration.class
}, properties = {
    "cas.ticket.registry.hazelcast.cluster.discovery.zookeeper.url=localhost:2181",
    "cas.ticket.registry.core.enable-locking=true"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ZookeeperLockRegistryTests {

    @Autowired
    @Qualifier("casTicketRegistryLockRepository")
    private LockRepository casTicketRegistryLockRepository;

    @Test
    public void verifyRepository() throws Exception {
        val lockKey = UUID.randomUUID().toString();

        val container = new Container();
        container.values.put(lockKey, new ArrayList<>());

        val threads = new ArrayList<Thread>();
        IntStream.range(0, 10).forEach(i -> {
            val thread = new Thread(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    Thread.sleep(250);
                    casTicketRegistryLockRepository.execute(lockKey, () -> {
                        container.values.get(lockKey).add(UUID.randomUUID().toString());
                        return null;
                    });
                }
            });
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

    private static class Container {
        private final Map<String, List<String>> values = new HashMap<>();
    }
}
