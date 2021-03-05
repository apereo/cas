package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link ImportRegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RestControllerEndpoint(id = "importRegisteredServices", enableByDefault = false)
public class ImportRegisteredServicesEndpoint extends BaseCasActuatorEndpoint {
    private final ServicesManager servicesManager;

    private final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers;

    public ImportRegisteredServicesEndpoint(final CasConfigurationProperties casProperties,
                                            final ServicesManager servicesManager,
                                            final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers) {
        super(casProperties);
        this.servicesManager = servicesManager;
        this.registeredServiceSerializers = registeredServiceSerializers;
    }

    /**
     * Import services.
     *
     * @param request the request
     * @return the http status
     * @throws Exception the exception
     */
    @PostMapping(consumes = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public HttpStatus importService(final HttpServletRequest request) throws Exception {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted registered service:\n[{}]", requestBody);

        val status = new AtomicInteger();
        registeredServiceSerializers
            .stream()
            .map(serializer -> serializer.from(requestBody))
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresentOrElse(service -> {
                LOGGER.trace("Storing registered service:\n[{}]", service);
                servicesManager.save(service);
                status.set(HttpStatus.CREATED.value());
            }, () -> status.set(HttpStatus.BAD_REQUEST.value()));
        return HttpStatus.valueOf(status.get());
    }
}
