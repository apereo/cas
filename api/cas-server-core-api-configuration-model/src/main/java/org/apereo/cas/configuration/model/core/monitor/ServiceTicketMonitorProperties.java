package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link ServiceTicketMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ServiceTicketMonitorProperties")
public class ServiceTicketMonitorProperties implements Serializable {

    private static final long serialVersionUID = -8167395674267219982L;

    /**
     * Warning settings for this monitor.
     */
    @NestedConfigurationProperty
    private MonitorWarningProperties warn = new MonitorWarningProperties(5000);
}
