package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link RedisAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-redis")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RedisAcceptableUsagePolicyProperties")
public class RedisAcceptableUsagePolicyProperties extends BaseRedisProperties {
    @Serial
    private static final long serialVersionUID = -2147683393318585262L;
}
