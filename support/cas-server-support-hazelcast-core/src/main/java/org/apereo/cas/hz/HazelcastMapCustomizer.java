package org.apereo.cas.hz;

import com.hazelcast.config.NamedConfig;
import org.springframework.core.Ordered;

/**
 * This is {@link HazelcastMapCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
public interface HazelcastMapCustomizer extends Ordered {
    /**
     * Customize map config.
     *
     * @param mapConfig the map config
     */
    void customize(NamedConfig mapConfig);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
