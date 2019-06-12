package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;

import java.util.Collection;

/**
 * This is {@link RegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "registeredServices", enableByDefault = false)
@Slf4j
public class RegisteredServicesEndpoint extends BaseCasActuatorEndpoint {
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
    @ReadOperation(produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public Collection<RegisteredService> handle() {
        return this.servicesManager.load();
    }

    /**
     * Fetch service either by numeric id or service id pattern.
     *
     * @param id the id
     * @return the registered service
     */
    @ReadOperation(produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public RegisteredService fetchService(@Selector final String id) {
        if (NumberUtils.isDigits(id)) {
            return this.servicesManager.findServiceBy(Long.parseLong(id));
        }
        return this.servicesManager.findServiceBy(id);
    }

    /**
     * Delete registered service.
     *
     * @param id the id
     * @return the registered service
     */
    @DeleteOperation(produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public RegisteredService deleteService(@Selector final String id) {
        if (NumberUtils.isDigits(id)) {
            val svc = this.servicesManager.findServiceBy(Long.parseLong(id));
            if (svc != null) {
                return this.servicesManager.delete(svc);
            }
        } else {
            val svc = this.servicesManager.findServiceBy(id);
            if (svc != null) {
                return this.servicesManager.delete(svc);
            }
        }
        LOGGER.warn("Could not locate service definition by id [{}]", id);
        return null;
    }
}
