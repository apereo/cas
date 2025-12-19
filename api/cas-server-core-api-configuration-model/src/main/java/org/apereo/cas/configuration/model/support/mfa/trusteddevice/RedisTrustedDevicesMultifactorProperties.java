package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import module java.base;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RedisTrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-trusted-mfa-redis")

public class RedisTrustedDevicesMultifactorProperties extends BaseRedisProperties {
    @Serial
    private static final long serialVersionUID = -2261683393319585262L;
}
