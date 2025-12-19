package org.apereo.cas.configuration.model.core.events;

import module java.base;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RedisEventsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-events-redis")
@Getter
@Setter
@Accessors(chain = true)
public class RedisEventsProperties extends BaseRedisProperties {
    @Serial
    private static final long serialVersionUID = 9027696961101634818L;
}
