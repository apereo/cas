package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("RedisSentinelProperties")
public class RedisSentinelProperties implements Serializable {
    private static final long serialVersionUID = 5434823157764550831L;

    /**
     * Name of Redis server.
     */
    private String master;

    /**
     * list of host:port pairs.
     */
    private List<String> node = new ArrayList<>(0);
}
