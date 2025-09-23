package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link RedisGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth-redis")
@Getter
@Setter
@Accessors(chain = true)
public class RedisGoogleAuthenticatorMultifactorProperties extends BaseRedisProperties {
    @Serial
    private static final long serialVersionUID = -1260683393319585262L;
}

