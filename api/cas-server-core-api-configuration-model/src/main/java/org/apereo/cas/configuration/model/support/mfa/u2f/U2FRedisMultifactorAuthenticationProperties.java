package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FRedisMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f-redis")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("U2FRedisMultifactorAuthenticationProperties")
public class U2FRedisMultifactorAuthenticationProperties extends BaseRedisProperties {
    private static final long serialVersionUID = -1261683393319585262L;
}
