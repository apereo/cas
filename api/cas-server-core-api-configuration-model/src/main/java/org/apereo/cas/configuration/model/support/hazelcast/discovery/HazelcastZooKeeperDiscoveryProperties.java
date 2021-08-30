package org.apereo.cas.configuration.model.support.hazelcast.discovery;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HazelcastZooKeeperDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-discovery-zookeeper")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastZooKeeperDiscoveryProperties")
public class HazelcastZooKeeperDiscoveryProperties implements Serializable {
    private static final long serialVersionUID = 235372431457637272L;

    @RequiredProperty
    private String url;

    @RequiredProperty
    private String group;

    @RequiredProperty
    private String path;
}
