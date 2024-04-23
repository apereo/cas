package org.apereo.cas.throttle;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.ThrottledSubmission;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link ConcurrentThrottledSubmissionsStore}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class ConcurrentThrottledSubmissionsStore extends BaseMappableThrottledSubmissionsStore<ThrottledSubmission> {
    public ConcurrentThrottledSubmissionsStore(final CasConfigurationProperties casProperties) {
        super(new ConcurrentHashMap<>(), casProperties);
    }
}
