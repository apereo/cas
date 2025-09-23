package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link RedisSentinelProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-redis-core")

public class RedisSentinelProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 5434823157764550831L;

    /**
     * Name of Redis server.
     */
    @RequiredProperty
    private String master;

    /**
     * Login password of the sentinel server.
     */
    private String password;

    /**
     * list of host:port pairs.
     */
    private List<String> node = new ArrayList<>();
}
