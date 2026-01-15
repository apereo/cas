package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.BaseMappableThrottledSubmissionsStore;
import com.hazelcast.map.IMap;
import lombok.val;

/**
 * This is {@link HazelcastMapThrottledSubmissionsStore}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class HazelcastMapThrottledSubmissionsStore<T extends ThrottledSubmission> extends BaseMappableThrottledSubmissionsStore<T> {
    public HazelcastMapThrottledSubmissionsStore(final Map<String, T> backingMap,
                                                 final CasConfigurationProperties casProperties) {
        super(backingMap, casProperties);
    }

    @Override
    public void removeIf(final Predicate<T> condition) {
        val hzMap = (IMap<String, T>) backingMap;
        hzMap.removeAll((com.hazelcast.query.Predicate<String, T>) entry -> condition.test(entry.getValue()));
    }
}
