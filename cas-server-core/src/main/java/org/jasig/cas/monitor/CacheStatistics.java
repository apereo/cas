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
 * Describes the simplest set of cache statistics that are meaningful for health monitoring.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public interface CacheStatistics {

    /**
     * Gets the current size of the cache in a unit specific to the cache being monitored (e.g. bytes, items, etc).
     *
     * @return Current cache size.
     */
    long getSize();


    /**
     * Gets the current capacity of the cache in a unit specific to the cache being monitored (e.g. bytes, items, etc).
     *
     * @return Current cache capacity.
     */
    long getCapacity();


    /**
     * Gets the number of items evicted from the cache in order to make space for new items.
     *
     * @return Eviction count.
     */
    long getEvictions();


    /**
     * Gets the percent free capacity remaining in the cache.
     *
     * @return Percent of space/capacity free.
     */
    int getPercentFree();


    /**
     * Gets a descriptive name of the cache instance for which statistics apply.
     *
     * @return Name of cache instance/host to which statistics apply.
     */
    String getName();


    /**
     * Writes a string representation of cache statistics to the given string builder.
     *
     * @param builder String builder to which string representation is appended.
     */
    void toString(StringBuilder builder);

}
