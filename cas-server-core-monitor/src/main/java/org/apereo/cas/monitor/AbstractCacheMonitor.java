package org.apereo.cas.monitor;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * Abstract base class for monitors that observe cache storage systems.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public abstract class AbstractCacheMonitor extends AbstractNamedMonitor<CacheStatus> {

    /**
     * CAS properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Override
    public CacheStatus observe() {
        CacheStatus status;
        try {
            final CacheStatistics[] statistics = getStatistics();
            if (statistics == null || statistics.length == 0) {
                return new CacheStatus(StatusCode.ERROR, "Cache statistics not available.");
            }
            final StatusCode[] overall = {StatusCode.OK};
            Arrays.stream(statistics).map(this::status)
                    .filter(code -> code.value() > overall[0].value())
                    .forEach(code -> overall[0] = code);
            status = new CacheStatus(overall[0], null, statistics);
        } catch (final Exception e) {
            status = new CacheStatus(e);
        }
        return status;
    }


    /**
     * Gets the statistics from this monitor.
     *
     * @return the statistics
     */
    protected abstract CacheStatistics[] getStatistics();


    /**
     * Computes the status code for a given set of cache statistics.
     *
     * @param statistics Cache statistics.
     * @return {@link StatusCode#WARN} if eviction count is above threshold or if
     * percent free space is below threshold, otherwise {@link StatusCode#OK}.
     */
    protected StatusCode status(final CacheStatistics statistics) {
        final StatusCode code;
        if (statistics.getEvictions() > casProperties.getMonitor().getWarn().getEvictionThreshold()) {
            code = StatusCode.WARN;
        } else if (statistics.getPercentFree() < casProperties.getMonitor().getWarn().getThreshold()) {
            code = StatusCode.WARN;
        } else {
            code = StatusCode.OK;
        }
        return code;
    }


}
