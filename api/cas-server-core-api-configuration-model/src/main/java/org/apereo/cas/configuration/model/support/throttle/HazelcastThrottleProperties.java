package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Configuration properties class for cas.throttle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-throttle-hazelcast")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastThrottleProperties")
public class HazelcastThrottleProperties extends BaseHazelcastProperties {
    private static final long serialVersionUID = 5813165633105563813L;
}
