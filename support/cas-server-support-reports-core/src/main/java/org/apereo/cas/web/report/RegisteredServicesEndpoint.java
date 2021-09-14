package org.apereo.cas.web.report;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

/**
 * This is {@link RegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RestControllerEndpoint(id = "registeredServices", enableByDefault = false)
@Slf4j
public class RegisteredServicesEndpoint extends BaseCasActuatorEndpoint {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers;

    public RegisteredServicesEndpoint(final CasConfigurationProperties casProperties, final ServicesManager servicesManager,
                                      final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                      final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers) {
        super(casProperties);
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.registeredServiceSerializers = registeredServiceSerializers;
    }

    /**
     * Handle and produce a list of services from registry.
     *
     * @return collection of services
     * @throws Exception the exception
     */
    @Operation(summary = "Handle and produce a list of services from registry")
    @GetMapping(produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> handle() throws Exception {
        return ResponseEntity.ok(MAPPER.writeValueAsString(servicesManager.load()));
    }

    /**
     * Fetch service either by numeric id or service id pattern.
     *
     * @param id the id
     * @return the registered service
     * @throws Exception the exception
     */
    @Operation(summary = "Fetch service either by numeric id or service id pattern",
        parameters = {@Parameter(name = "id", required = true)})
    @GetMapping(path = "{id}", produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> fetchService(@PathVariable final String id) throws Exception {
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
     * @throws Exception the exception
     */
    @Operation(summary = "Delete registered service by id", parameters = {@Parameter(name = "id", required = true)})
    @DeleteMapping(path = "{id}", produces = {ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteService(@PathVariable final String id) throws Exception {
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

    /**
     * Import services.
     *
     * @param request the request
     * @return the http status
     * @throws Exception the exception
     */
    @PostMapping(path = "/import", consumes = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml",
        MediaType.APPLICATION_JSON_VALUE
    })
    @Operation(summary = "Import registered services as a JSON document or a zip file")
    public ResponseEntity<RegisteredService> importService(final HttpServletRequest request) throws Exception {
        val contentType = request.getContentType();
        if (StringUtils.equalsAnyIgnoreCase(MediaType.APPLICATION_OCTET_STREAM_VALUE, contentType)) {
            return importServicesAsStream(request);
        }
        return importSingleService(request);
    }

    /**
     * Export.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export registered services as a zip file")
    public ResponseEntity<Resource> export() {
        val serializer = new RegisteredServiceJsonSerializer();
        val resource = CompressionUtils.toZipFile(servicesManager.stream(),
            Unchecked.function(entry -> {
                val service = (RegisteredService) entry;
                val fileName = String.format("%s-%s", service.getName(), service.getId());
                val sourceFile = File.createTempFile(fileName, ".json");
                serializer.to(sourceFile, service);
                return sourceFile;
            }), "services");
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private ResponseEntity<RegisteredService> importSingleService(final HttpServletRequest request) throws IOException {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted registered service:\n[{}]", requestBody);

        if (StringUtils.isBlank(requestBody)) {
            LOGGER.warn("Could not extract registered services from request body");
            return ResponseEntity.badRequest().build();
        }

        return registeredServiceSerializers
            .stream()
            .map(serializer -> serializer.from(requestBody))
            .filter(Objects::nonNull)
            .findFirst()
            .map(service -> {
                LOGGER.trace("Storing registered service:\n[{}]", service);
                return servicesManager.save(service);
            })
            .map(service -> {
                val headers = new HttpHeaders();
                headers.put("id", CollectionUtils.wrapList(String.valueOf(service.getId())));
                return new ResponseEntity<>(service, headers, HttpStatus.CREATED);
            })
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    private ResponseEntity<RegisteredService> importServicesAsStream(final HttpServletRequest request) throws IOException {
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
        return ResponseEntity.ok().build();
    }
}
