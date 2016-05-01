package org.jasig.cas.monitor;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;
import org.jasig.cas.config.HazelcastProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link HazelcastMonitor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("hazelcastMonitor")
public class HazelcastMonitor extends AbstractCacheMonitor {

    @Autowired
    @Qualifier("hazelcastProperties")
    private HazelcastProperties hazelcastProperties;

    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> statsList = new ArrayList<>();

        final HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(this.hazelcastProperties.getInstanceName());
        final IMap map = instance.getMap(this.hazelcastProperties.getMapName());
        statsList.add(new HazelcastStatistics(map));

        return statsList.toArray(new CacheStatistics[statsList.size()]);
    }

    /**
     * The type Hazelcast statistics.
     */
    public static class HazelcastStatistics implements CacheStatistics {
        private static final int PERCENTAGE_VALUE = 100;

        private IMap map;

        protected HazelcastStatistics(final IMap map) {
            this.map = map;
        }

        @Override
        public long getSize() {
            return this.map.size();
        }

        @Override
        public long getCapacity() {
            return this.map.getLocalMapStats().total();
        }

        @Override
        public long getEvictions() {
            return this.map.getLocalMapStats().getNearCacheStats().getMisses();
        }

        @Override
        public String getName() {
            return this.map.getName();
        }

        @Override
        public int getPercentFree() {
            final long capacity = getCapacity();
            if (capacity == 0) {
                return 0;
            }
            return (int) ((capacity - getSize()) * PERCENTAGE_VALUE / capacity);
        }

        @Override
        public void toString(final StringBuilder builder) {
            final LocalMapStats localMapStats = map.getLocalMapStats();

            builder.append("Creation time: ")
                    .append(localMapStats.getCreationTime())
                    .append(',')
                    .append("Owned entry count: ")
                    .append(localMapStats.getOwnedEntryCount())
                    .append(',')
                    .append("Backup entry count: ")
                    .append(localMapStats.getBackupEntryCount())
                    .append(',')
                    .append("Backup count: ")
                    .append(localMapStats.getBackupCount())
                    .append(',')
                    .append("Hits count: ")
                    .append(localMapStats.getHits())
                    .append(',')
                    .append("Last update time: ")
                    .append(localMapStats.getLastUpdateTime())
                    .append(',')
                    .append("Last access time: ")
                    .append(localMapStats.getLastAccessTime())
                    .append(',')
                    .append("Locked entry count: ")
                    .append(localMapStats.getLockedEntryCount())
                    .append(',')
                    .append("Dirty entry count: ")
                    .append(localMapStats.getDirtyEntryCount())
                    .append(',')
                    .append("Total get latency: ")
                    .append(localMapStats.getMaxGetLatency())
                    .append(',')
                    .append("Total put latency: ")
                    .append(localMapStats.getTotalPutLatency())
                    .append(',')
                    .append("Total remove latency: ")
                    .append(localMapStats.getTotalRemoveLatency())
                    .append(',')
                    .append("Heap cost: ")
                    .append(localMapStats.getHeapCost())
                    .append(',')
                    .append("Misses: ")
                    .append(localMapStats.getNearCacheStats().getMisses());
        }
    }
}
