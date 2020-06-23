package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link RedisClusterNodeProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-redis-core")
public class RedisClusterNodeProperties implements Serializable {
    private static final long serialVersionUID = 2912983343579258662L;

    /**
     * Server's host address.
     */
    @RequiredProperty
    private String host;

    /**
     * Server's port number.
     */
    @RequiredProperty
    private int port;

    /**
     * Set the id of the master node.
     */
    @RequiredProperty
    private String replicaOf;

    /**
     * Identifier of this node.
     */
    private String id;

    /**
     * Name of this node.
     */
    private String name;

    /**
     * Indicate the type/role of this node.
     * Accepted values are: {@code MASTER, SLAVE}.
     */
    @RequiredProperty
    private String type;
}
