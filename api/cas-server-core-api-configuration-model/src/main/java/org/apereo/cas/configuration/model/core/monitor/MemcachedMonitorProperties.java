package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MemcachedMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-memcached-monitor")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MemcachedMonitorProperties")
public class MemcachedMonitorProperties extends BaseMemcachedProperties {
    private static final long serialVersionUID = -9139788158851782673L;
}
