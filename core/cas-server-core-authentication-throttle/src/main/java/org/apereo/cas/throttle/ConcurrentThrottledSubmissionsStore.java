package org.apereo.cas.throttle;

import org.apereo.cas.web.support.ThrottledSubmission;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link ConcurrentThrottledSubmissionsStore}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class ConcurrentThrottledSubmissionsStore extends BaseMappableThrottledSubmissionsStore<ThrottledSubmission> {
    public ConcurrentThrottledSubmissionsStore() {
        super(new ConcurrentHashMap<>());
    }
}
