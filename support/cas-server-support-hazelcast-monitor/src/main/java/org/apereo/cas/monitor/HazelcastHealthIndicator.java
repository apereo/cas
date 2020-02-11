package org.apereo.cas.monitor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceProxy;
import com.hazelcast.internal.memory.MemoryStats;
import com.hazelcast.map.IMap;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;

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

    public HazelcastHealthIndicator(final long evictionThreshold, final long threshold,
                                    final HazelcastInstance instance) {
        super(evictionThreshold, threshold);
        this.instance = (HazelcastInstanceProxy) instance;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        val stats = instance.getConfig().getMapConfigs().keySet();
        val statsList = new ArrayList<CacheStatistics>(stats.size());
        stats.forEach(key -> {
            val map = instance.getMap(key);
            val memoryStats = instance.getOriginal().getMemoryStats();
            LOGGER.debug("Starting to collect hazelcast statistics for map [{}] identified by key [{}]...", map, key);
            statsList.add(new HazelcastStatistics(map, instance.getCluster().getMembers().size(), memoryStats));
        });
        return statsList.toArray(CacheStatistics[]::new);
    }

    /**
     * The type Hazelcast statistics.
     */
    public static class HazelcastStatistics implements CacheStatistics {

        private static final int PERCENTAGE_VALUE = 100;

        private final IMap map;
        private final long clusterSize;

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
            val localMapStats = map.getLocalMapStats();
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
