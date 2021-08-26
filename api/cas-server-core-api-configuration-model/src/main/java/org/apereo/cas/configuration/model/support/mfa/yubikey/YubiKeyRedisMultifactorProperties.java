package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link YubiKeyRedisMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-yubikey-redis")
@Getter
@Setter
@Accessors(chain = true)
public class YubiKeyRedisMultifactorProperties extends BaseRedisProperties {
    private static final long serialVersionUID = -1261683393319585262L;
}
