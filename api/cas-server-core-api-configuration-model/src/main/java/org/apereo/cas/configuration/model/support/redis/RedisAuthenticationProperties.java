package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Redis.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-redis-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class RedisAuthenticationProperties extends BaseRedisProperties {

    private static final long serialVersionUID = -1232996050439638782L;

    /**
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * Password encoder settings for this handler.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Order of authentication handler in chain.
     */
    private int order = Integer.MAX_VALUE;
}
