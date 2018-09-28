package org.apereo.cas.monitor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.memory.MemoryStats;
import com.hazelcast.monitor.LocalMapStats;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link HazelcastHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
public class HazelcastHealthIndicator extends AbstractCacheHealthIndicator {

    /**
     * CAS Hazelcast Instance.
     */
    private final HazelcastInstanceProxy instance;

    public HazelcastHealthIndicator(final CasConfigurationProperties casProperties,
                                    final HazelcastInstance instance) {
        super(casProperties);
        this.instance = (HazelcastInstanceProxy) instance;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> statsList = new ArrayList<>();
        instance.getConfig().getMapConfigs().keySet().forEach(key -> {
            final IMap map = instance.getMap(key);
            final MemoryStats memoryStats = instance.getOriginal().getMemoryStats();
            LOGGER.debug("Starting to collect hazelcast statistics for map [{}] identified by key [{}]...", map, key);
            statsList.add(new HazelcastStatistics(map, instance.getCluster().getMembers().size(), memoryStats));
        });
        return statsList.toArray(new CacheStatistics[0]);
    }

    /**
     * The type Hazelcast statistics.
     */
    public static class HazelcastStatistics implements CacheStatistics {

        private static final int PERCENTAGE_VALUE = 100;

        private final IMap map;

        private final int clusterSize;

        private final MemoryStats memoryStats;

        protected HazelcastStatistics(final IMap map, final int clusterSize, final MemoryStats memoryStats) {
            this.map = map;
            this.clusterSize = clusterSize;
            this.memoryStats = memoryStats;
        }

        @Override
        public long getSize() {
            return this.map.size();
        }

        @Override
        public long getCapacity() {
            return this.memoryStats.getCommittedHeap();
        }

        @Override
        public long getEvictions() {
            if (this.map.getLocalMapStats() != null && this.map.getLocalMapStats().getNearCacheStats() != null) {
                return this.map.getLocalMapStats().getNearCacheStats().getMisses();
            }
            return 0;
        }

        @Override
        public String getName() {
            return this.map.getName();
        }

        @Override
        public long getPercentFree() {
            return this.memoryStats.getFreeHeap() * PERCENTAGE_VALUE / this.memoryStats.getCommittedHeap();
        }

        @Override
        public String toString(final StringBuilder builder) {
            final LocalMapStats localMapStats = map.getLocalMapStats();
            builder.append("Creation time: ")
                .append(localMapStats.getCreationTime())
                .append(", Cluster size: ")
                .append(clusterSize)
                .append(", Owned entry count: ")
                .append(localMapStats.getOwnedEntryCount())
                .append(", Backup entry count: ")
                .append(localMapStats.getBackupEntryCount())
                .append(", Backup count: ")
                .append(localMapStats.getBackupCount())
                .append(", Hits count: ")
                .append(localMapStats.getHits())
                .append(", Last update time: ")
                .append(localMapStats.getLastUpdateTime())
                .append(", Last access time: ")
                .append(localMapStats.getLastAccessTime())
                .append(", Locked entry count: ")
                .append(localMapStats.getLockedEntryCount())
                .append(", Dirty entry count: ")
                .append(localMapStats.getDirtyEntryCount())
                .append(", Total get latency: ")
                .append(localMapStats.getMaxGetLatency())
                .append(", Total put latency: ")
                .append(localMapStats.getTotalPutLatency())
                .append(", Total remove latency: ")
                .append(localMapStats.getTotalRemoveLatency())
                .append(", Heap cost: ")
                .append(localMapStats.getHeapCost());
            if (localMapStats.getNearCacheStats() != null) {
                builder.append(", Misses: ").append(localMapStats.getNearCacheStats().getMisses());
            }
            return builder.toString();
        }
    }
}
