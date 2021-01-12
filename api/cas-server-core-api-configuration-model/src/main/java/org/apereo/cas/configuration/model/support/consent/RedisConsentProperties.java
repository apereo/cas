package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RedisConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-redis")
@Getter
@Setter
@Accessors(chain = true)
public class RedisConsentProperties extends BaseRedisProperties {
    private static final long serialVersionUID = -1347683393318585262L;
}
