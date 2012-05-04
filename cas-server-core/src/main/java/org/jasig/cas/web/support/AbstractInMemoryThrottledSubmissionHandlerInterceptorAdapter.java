/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.web.support;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private final ConcurrentMap<String, AtomicInteger> ipMap = new ConcurrentHashMap<String, AtomicInteger>();

    @Override
    protected final int findCount(final HttpServletRequest request, final String usernameParameter, final int failureRangeInSeconds) {
        final AtomicInteger existingValue = this.ipMap.get(constructKey(request, usernameParameter));
        return existingValue == null ? 0 : existingValue.get();
    }

    @Override
    protected final void updateCount(final HttpServletRequest request, final String usernameParameter) {
        final AtomicInteger newAtomicInteger = new AtomicInteger(1);
        final AtomicInteger oldAtomicInteger = this.ipMap.putIfAbsent(constructKey(request, usernameParameter), newAtomicInteger);

        if (oldAtomicInteger != null) {
            oldAtomicInteger.incrementAndGet();
        }
    }

    protected abstract String constructKey(HttpServletRequest request, String usernameParameter);

    /**
     * This class relies on an external configuration to clean it up. It ignores the threshold data in the parent class.
     */
    public final void decrementCounts() {
        final Set<String> keys = this.ipMap.keySet();
        log.debug("Decrementing counts for throttler.  Starting key count: " + keys.size());

        for (final Iterator<String> iter = keys.iterator(); iter.hasNext();) {
            final String key = iter.next();
            final AtomicInteger integer = this.ipMap.get(key);
            final int  newValue = integer.decrementAndGet();

            log.trace("Decrementing count for key [" + key + "]; starting count [" + integer + "]; ending count [" + newValue + "]");

            if (newValue == 0) {
                iter.remove();
            }
        }
        log.debug("Done decrementing count for throttler.");
    }
}
