package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link RedisClusterProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-redis-core")
public class RedisClusterProperties implements Serializable {
    private static final long serialVersionUID = 5236837157740950831L;

    /**
     * List of nodes available in the redis cluster.
     */
    private List<RedisClusterNodeProperties> nodes = new ArrayList<>(0);

    /**
     * The cluster connection's password.
     */
    private String password;

    /**
     * The max number of redirects to follow.
     */
    private int maxRedirects;
}
