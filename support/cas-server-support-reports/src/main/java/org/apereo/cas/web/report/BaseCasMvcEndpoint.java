package org.apereo.cas.web.report;

import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.springframework.boot.actuate.endpoint.mvc.AbstractNamedMvcEndpoint;

/**
 * This is {@link BaseCasMvcEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseCasMvcEndpoint extends AbstractNamedMvcEndpoint {

    public BaseCasMvcEndpoint(final String name, final String path, final MonitorProperties.Endpoints.BaseEndpoint endpoint) {
        super(name, path, endpoint.isSensitive(), endpoint.isEnabled());
    }
}
