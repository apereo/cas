package org.apereo.cas.configuration.model.core.monitor;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JsonSecurityActuatorEndpointsMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Accessors(chain = true)
public class JsonSecurityActuatorEndpointsMonitorProperties extends SpringResourceProperties {

    @Serial
    private static final long serialVersionUID = -1573755681498251679L;
}
