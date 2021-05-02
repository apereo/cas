package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * This is {@link ExportRegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "exportRegisteredServices", enableByDefault = false)
public class ExportRegisteredServicesEndpoint extends BaseCasActuatorEndpoint {
    private final ServicesManager servicesManager;

    public ExportRegisteredServicesEndpoint(final CasConfigurationProperties casProperties,
                                            final ServicesManager servicesManager) {
        super(casProperties);
        this.servicesManager = servicesManager;
    }

    /**
     * Export services web endpoint response.
     *
     * @return the web endpoint response
     */
    @ReadOperation
    public WebEndpointResponse<Resource> exportServices() {
        val serializer = new RegisteredServiceJsonSerializer();
        val resource = CompressionUtils.toZipFile(servicesManager.stream(),
            Unchecked.function(entry -> {
                val service = (RegisteredService) entry;
                val fileName = String.format("%s-%s", service.getName(), service.getId());
                val sourceFile = File.createTempFile(fileName, ".json");
                serializer.to(sourceFile, service);
                return sourceFile;
            }), "services");
        return new WebEndpointResponse<>(resource);
    }
}
