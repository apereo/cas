package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.cloud.context.refresh.ContextRefresher;

import java.util.Set;

/**
 * This is {@link CasApplicationContextReloadEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Endpoint(id = "reloadContext", enableByDefault = false)
public class CasApplicationContextReloadEndpoint extends BaseCasActuatorEndpoint {
    private final ContextRefresher contextRefresher;

    public CasApplicationContextReloadEndpoint(final CasConfigurationProperties casProperties,
                                               final ContextRefresher contextRefresher) {
        super(casProperties);
        this.contextRefresher = contextRefresher;
    }

    /**
     * Reload the application context
     * using Spring Cloud {@link ContextRefresher}.
     *
     * @return the set of properties that might have been refreshed, if any.
     */
    @WriteOperation
    public Set<String> reload() {
        return contextRefresher.refresh();
    }
}
