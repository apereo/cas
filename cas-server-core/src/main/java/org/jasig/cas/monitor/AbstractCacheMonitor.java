/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.monitor;

/**
 * Abstract base class for monitors that observe cache storage systems.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public abstract class AbstractCacheMonitor extends AbstractNamedMonitor<CacheStatus> {

    /** Default free capacity threshold is 10%. */
    public static final int DEFAULT_WARN_FREE_THRESHOLD = 10;

    /** Default eviction threshold is 0. */
    public static final long DEFAULT_EVICTION_THRESHOLD = 0;

    /** Percent free capacity threshold below which a warning is issued.*/
    private int warnFreeThreshold = DEFAULT_WARN_FREE_THRESHOLD;

    /** Threshold for number of acceptable evictions above which an error is issued. */
    private long evictionThreshold = DEFAULT_EVICTION_THRESHOLD;


    /**
     * Sets the percent free capacity threshold below which a warning is issued.
     *
     * @param percent Warning threshold percent.
     */
    public void setWarnFreeThreshold(final int percent) {
        this.warnFreeThreshold = percent;
    }


    /**
     * Sets the eviction threshold count above which an error is issued.
     *
     * @param count Threshold for number of cache evictions.
     */
    public void setEvictionThreshold(final long count) {
        this.evictionThreshold = count;
    }

    @Override
    public CacheStatus observe() {
        CacheStatus status;
        try {
            final CacheStatistics[] statistics = getStatistics();
            if (statistics == null || statistics.length == 0) {
                return new CacheStatus(StatusCode.ERROR, "Cache statistics not available.");
            }
            StatusCode overall = StatusCode.OK;
            StatusCode code;
            for (final CacheStatistics stats : statistics) {
                code = status(stats);
                // Record highest status which is equivalent to worst case
                if (code.value() > overall.value()) {
                    overall = code;
                }
            }
            status = new CacheStatus(overall, null, statistics);
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
     *
     * @return {@link StatusCode#WARN} if eviction count is above threshold or if
     * percent free space is below threshold, otherwise {@link StatusCode#OK}.
     */
    protected StatusCode status(final CacheStatistics statistics) {
        final StatusCode code;
        if (statistics.getEvictions() > this.evictionThreshold) {
            code = StatusCode.WARN;
        } else if (statistics.getPercentFree() < this.warnFreeThreshold) {
            code = StatusCode.WARN;
        } else {
            code = StatusCode.OK;
        }
        return code;
    }
}
