package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link WebAuthnRedisMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-webauthn-redis")
@Getter
@Setter
@Accessors(chain = true)
public class WebAuthnRedisMultifactorProperties extends BaseRedisProperties {
    @Serial
    private static final long serialVersionUID = -2261683393319585262L;
}

