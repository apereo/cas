package org.apereo.cas.hz;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.function.FunctionUtils;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link HazelcastClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class HazelcastClusterTopologyManager implements ClusterTopologyManager {
    private final HazelcastInstance hazelcastInstance;

    @Override
    public List<? extends ClusterMember> discoverMembers() {
        val executor = hazelcastInstance.getExecutorService("hazelcast-cluster-" + hazelcastInstance.getName());
        return hazelcastInstance.getCluster()
            .getMembers()
            .parallelStream()
            .map(member -> {
                val startedAt = System.nanoTime();
                return FunctionUtils.doAndHandle(
                        () -> {
                            executor.submitToMember(new PingTask(), member).get(5, TimeUnit.SECONDS);

                            return ClusterMember.builder()
                                .owner(getName())
                                .id(member.getUuid().toString())
                                .address(member.getAddress().toString())
                                .responseTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt))
                                .status(true)
                                .attributes(Map.of("instanceName", hazelcastInstance.getName(), "local", member.localMember()))
                                .build();
                        },
                        e -> ClusterMember.builder()
                            .owner(getName())
                            .id(member.getUuid().toString())
                            .address(member.getAddress().toString())
                            .responseTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt))
                            .status(false)
                            .description("%s: %s".formatted(
                                e.getClass().getSimpleName(),
                                e.getMessage()))
                            .attributes(Map.of("instanceName", hazelcastInstance.getName(), "local", member.localMember()))
                            .build())
                    .get();
            })
            .toList();
    }

    private static final class PingTask implements Callable<String>, Serializable {
        @Serial
        private static final long serialVersionUID = 2770449444321644053L;

        @Override
        public String call() {
            return "OK";
        }
    }
}
