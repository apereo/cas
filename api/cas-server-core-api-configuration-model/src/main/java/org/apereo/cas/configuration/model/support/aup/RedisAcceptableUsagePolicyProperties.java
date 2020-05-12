package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
public class RedisAcceptableUsagePolicyProperties extends BaseRedisProperties {
    private static final long serialVersionUID = -2147683393318585262L;
}
