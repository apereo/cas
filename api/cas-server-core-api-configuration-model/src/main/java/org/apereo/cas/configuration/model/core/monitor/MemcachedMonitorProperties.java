package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link MemcachedMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated Since 7.0.0
 */
@RequiresModule(name = "cas-server-support-memcached-monitor")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MemcachedMonitorProperties")
@Deprecated(since = "7.0.0")
public class MemcachedMonitorProperties extends BaseMemcachedProperties {
    @Serial
    private static final long serialVersionUID = -9139788158851782673L;
}
