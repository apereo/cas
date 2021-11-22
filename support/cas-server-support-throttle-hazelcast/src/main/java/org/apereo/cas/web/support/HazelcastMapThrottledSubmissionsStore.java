package org.apereo.cas.web.support;

import org.apereo.cas.throttle.BaseMappableThrottledSubmissionsStore;

import com.hazelcast.map.IMap;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This is {@link HazelcastMapThrottledSubmissionsStore}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class HazelcastMapThrottledSubmissionsStore extends BaseMappableThrottledSubmissionsStore {
    public HazelcastMapThrottledSubmissionsStore(final Map<String, ZonedDateTime> backingMap) {
        super(backingMap);
    }

    @Override
    public void removeIf(final Predicate<ThrottledSubmission> condition) {
        ((IMap<String, ZonedDateTime>) backingMap).removeAll(
            (com.hazelcast.query.Predicate<String, ZonedDateTime>)
                entry -> condition.test(new ThrottledSubmission(entry.getKey(), entry.getValue())));
    }
}
