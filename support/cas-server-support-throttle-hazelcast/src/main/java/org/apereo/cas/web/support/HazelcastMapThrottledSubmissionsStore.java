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

    /**
     * Removes the submissions that match the condition. The condition is evaluated here rather than sent to the
     * members that own the entries, since it is a plain Java predicate that cannot be serialized to them.
     *
     * @param condition the condition
     */
    @Override
    public void removeIf(final Predicate<T> condition) {
        val hzMap = (IMap<String, T>) backingMap;
        hzMap.entrySet()
            .stream()
            .filter(entry -> condition.test(entry.getValue()))
            .map(Map.Entry::getKey)
            .forEach(hzMap::delete);
    }
}
