package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

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
    @PostMapping(consumes = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml",
        MediaType.APPLICATION_JSON_VALUE
    })
    public HttpStatus importService(final HttpServletRequest request) throws Exception {
        val contentType = request.getContentType();
        if (StringUtils.equalsAnyIgnoreCase(MediaType.APPLICATION_OCTET_STREAM_VALUE, contentType)) {
            return importServicesAsStream(request);
        }
        return importSingleService(request);
    }

    private HttpStatus importServicesAsStream(final HttpServletRequest request) throws IOException {
        var servicesToImport = Stream.<RegisteredService>empty();
        try (val bais = new ByteArrayInputStream(IOUtils.toByteArray(request.getInputStream()));
             val zipIn = new ZipInputStream(bais)) {
            var entry = zipIn.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    val requestBody = IOUtils.toString(zipIn, StandardCharsets.UTF_8);
                    servicesToImport = Stream.concat(servicesToImport, registeredServiceSerializers
                        .stream()
                        .map(serializer -> serializer.from(requestBody))
                        .filter(Objects::nonNull));
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        servicesManager.save(servicesToImport);
        return HttpStatus.CREATED;
    }

    private HttpStatus importSingleService(final HttpServletRequest request) throws IOException {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted registered service:\n[{}]", requestBody);

        if (StringUtils.isBlank(requestBody)) {
            LOGGER.warn("Could not extract registered services from request body");
            return HttpStatus.BAD_REQUEST;
        }

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
