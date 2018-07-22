package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.BaseCasMvcEndpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Collection;

/**
 * This is {@link RegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "registered-services", enableByDefault = false)
public class RegisteredServicesEndpoint extends BaseCasMvcEndpoint {
    private final ServicesManager servicesManager;

    /**
     * Instantiates a new mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param casProperties   the cas properties
     * @param servicesManager the services manager
     */
    public RegisteredServicesEndpoint(final CasConfigurationProperties casProperties, final ServicesManager servicesManager) {
        super(casProperties);
        this.servicesManager = servicesManager;
    }

    /**
     * Handle and produce a list of services from registry.
     *
     * @return the web async task
     */
    @ReadOperation
    public Collection<RegisteredService> handle() {
        return this.servicesManager.getAllServices();
    }
}
