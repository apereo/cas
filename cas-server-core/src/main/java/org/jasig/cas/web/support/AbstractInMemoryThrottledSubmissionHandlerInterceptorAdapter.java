/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of a HandlerInterceptorAdapter that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 * <p>
 * Note, this class relies on an external method for decrementing the counts (i.e. a Quartz Job) and runs independent of the
 * threshold of the parent.

 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
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
