package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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
@JsonFilter("AuditRedisProperties")
public class AuditRedisProperties extends BaseRedisProperties {
    @Serial
    private static final long serialVersionUID = -8112996050439638782L;

    /**
     * Execute the recording of audit records in async manner.
     * This setting must almost always be set to true.
     */
    private boolean asynchronous = true;
}
