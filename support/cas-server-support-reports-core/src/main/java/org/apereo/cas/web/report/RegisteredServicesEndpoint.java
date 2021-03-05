package org.apereo.cas.web.report;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * This is {@link RegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "registeredServices", enableByDefault = false)
@Slf4j
public class RegisteredServicesEndpoint extends BaseCasActuatorEndpoint {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    public RegisteredServicesEndpoint(final CasConfigurationProperties casProperties, final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        super(casProperties);
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    /**
     * Handle and produce a list of services from registry.
     *
     * @return collection of services
     */
    @SneakyThrows
    @ReadOperation(produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> handle() {
        return ResponseEntity.ok(MAPPER.writeValueAsString(servicesManager.load()));
    }

    /**
     * Fetch service either by numeric id or service id pattern.
     *
     * @param id the id
     * @return the registered service
     */
    @SneakyThrows
    @ReadOperation(produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> fetchService(@Selector final String id) {
        val service = NumberUtils.isDigits(id)
            ? servicesManager.findServiceBy(Long.parseLong(id))
            : servicesManager.findServiceBy(webApplicationServiceFactory.createService(id));
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(MAPPER.writeValueAsString(service));
    }

    /**
     * Delete registered service.
     *
     * @param id the id
     * @return the registered service
     */
    @SneakyThrows
    @DeleteOperation(produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteService(@Selector final String id) {
        if (NumberUtils.isDigits(id)) {
            val svc = servicesManager.findServiceBy(Long.parseLong(id));
            if (svc != null) {
                return ResponseEntity.ok(MAPPER.writeValueAsString(servicesManager.delete(svc)));
            }
        } else {
            val svc = servicesManager.findServiceBy(webApplicationServiceFactory.createService(id));
            if (svc != null) {
                return ResponseEntity.ok(MAPPER.writeValueAsString(servicesManager.delete(svc)));
            }
        }
        LOGGER.warn("Could not locate service definition by id [{}]", id);
        return ResponseEntity.notFound().build();
    }
}
