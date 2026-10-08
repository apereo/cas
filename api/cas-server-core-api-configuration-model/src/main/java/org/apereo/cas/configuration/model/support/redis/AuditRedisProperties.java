package org.apereo.cas.configuration.model.support.redis;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Configuration properties for Redis.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-audit-redis")
@Getter
@Setter
@Accessors(chain = true)
public class AuditRedisProperties extends BaseRedisProperties {
    @Serial
    private static final long serialVersionUID = -8112996050439638782L;

    /**
     * Execute the recording of audit records in async manner.
     * This setting must almost always be set to true.
     */
    private boolean asynchronous = true;

    /**
     * Indicates how long audit records should be kept in Redis.
     * Each audit record is stored with this value as its time-to-live,
     * and Redis removes it once that period has passed.
     * Set to {@code -1}, {@code INFINITE} or leave blank to keep audit records forever.
     * Only records written after this setting is applied carry an expiration.
     */
    @DurationCapable
    private String maxAge = "P180D";
}
