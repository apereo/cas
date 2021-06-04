package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("RedisClusterProperties")
public class RedisClusterProperties implements Serializable {
    private static final long serialVersionUID = 5236837157740950831L;

    /**
     * List of nodes available in the redis cluster.
     */
    private List<RedisClusterNodeProperties> nodes = new ArrayList<>(0);

    /**
     * The cluster connection's password.
     */
    @RequiredProperty
    private String password;

    /**
     * The max number of redirects to follow.
     */
    private int maxRedirects;

    /**
     * Whether to discover and query all cluster nodes for obtaining the
     * cluster topology. When set to false, only the initial seed nodes are
     * used as sources for topology discovery.
     */
    private boolean dynamicRefreshSources = true;

    /**
     * Enables periodic refresh of cluster topology and sets the refresh period.
     */
    @DurationCapable
    private String topologyRefreshPeriod;

    /**
     * Whether adaptive topology refreshing using all available refresh triggers should be used.
     */
    private boolean adaptiveTopologyRefresh;
}
