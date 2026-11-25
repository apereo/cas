package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.CacheableServicesManager;
import org.apereo.cas.services.ChainingServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.io.TemporaryFileSystemResource;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.google.common.primitives.Longs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link RegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "registeredServices", defaultAccess = Access.NONE)
@Slf4j
public class RegisteredServicesEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<ServicesManagerConfigurationContext> configurationContext;

    private final ObjectProvider<List<? extends StringSerializer<RegisteredService>>> registeredServiceSerializers;

    public RegisteredServicesEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<ServicesManagerConfigurationContext> configurationContext,
        final ObjectProvider<List<? extends StringSerializer<RegisteredService>>> registeredServiceSerializers,
        final ObjectProvider<ConfigurableApplicationContext> applicationContext) {
        super(casProperties, applicationContext.getObject());
        this.servicesManager = servicesManager;
        this.configurationContext = configurationContext;
        this.registeredServiceSerializers = registeredServiceSerializers;
    }

    /**
     * Handle and produce a list of services from registry.
     *
     * @return collection of services
     */
    @Operation(summary = "Handle and produce a list of services from registry")
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    public ResponseEntity<String> handle() {
        return ResponseEntity.ok(MAPPER.writeValueAsString(
            servicesManager.getObject()
                .load()
                .stream()
                .filter(RegisteredServiceProperty.RegisteredServiceProperties.INTERNAL_SERVICE_DEFINITION::isNotAssignedTo)
                .collect(Collectors.toList())
        ));
    }

    /**
     * Fetch service either by numeric id or service id pattern.
     *
     * @param id the id
     * @return the registered service
     */
    @Operation(summary = "Fetch service either by numeric id or service id pattern")
    @GetMapping(path = "{id}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    public ResponseEntity<String> fetchService(
        @PathVariable final String id) {
        val registeredServiceId = Longs.tryParse(id);
        val service = registeredServiceId != null
            ? servicesManager.getObject().findServiceBy(registeredServiceId)
            : servicesManager.getObject().findServiceBy(configurationContext.getObject().getServiceFactory().createService(id));
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(MAPPER.writeValueAsString(service));
    }

    /**
     * Fetch services by type response entity.
     *
     * @param type the simple name of the CAS service type (i.e {@link org.apereo.cas.services.CasRegisteredService}
     * @return the response entity
     */
    @Operation(summary = "Fetch services by their type")
    @GetMapping(path = "type/{type}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    public ResponseEntity<String> fetchServicesByType(
        @PathVariable final String type) {
        val services = servicesManager.getObject().findServiceBy(registeredService ->
            registeredService.getClass().getSimpleName().equalsIgnoreCase(type));
        return ResponseEntity.ok(MAPPER.writeValueAsString(services));
    }

    /**
     * Delete registered service cache.
     *
     * @return the response entity
     */
    @Operation(summary = "Delete cached services")
    @DeleteMapping(path = "/cache",
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_YAML_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    public ResponseEntity deleteCache() {
        val size = configurationContext.getObject().getServicesCache().estimatedSize();
        configurationContext.getObject().getServicesCache().invalidateAll();
        return ResponseEntity.ok(Map.of("deleted", size));
    }

    /**
     * Delete all services.
     *
     * @return the response entity
     */
    @Operation(summary = "Delete all registered services")
    @DeleteMapping(
        consumes = {
            MediaType.ALL_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_YAML_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    public ResponseEntity<String> deleteAllServices() {
        val manager = servicesManager.getObject();
        val effectiveManagers = manager instanceof final ChainingServicesManager chain
            ? List.copyOf(chain.getServiceManagers())
            : List.of(manager);

        effectiveManagers
            .stream()
            .filter(CacheableServicesManager.class::isInstance)
            .map(CacheableServicesManager.class::cast)
            .forEach(CacheableServicesManager::removeRegisteredServicesFromCache);
        manager.deleteAll();
        return ResponseEntity.ok().build();
    }

    /**
     * Delete registered service.
     *
     * @param id the id
     * @return the registered service
     */
    @Operation(summary = "Delete registered service by id")
    @DeleteMapping(path = "{id}",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_YAML_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    public ResponseEntity<String> deleteService(@PathVariable final String id) {
        val registeredServiceId = Longs.tryParse(id);
        if (registeredServiceId != null) {
            val svc = servicesManager.getObject().findServiceBy(registeredServiceId);
            if (svc != null) {
                return ResponseEntity.ok(MAPPER.writeValueAsString(servicesManager.getObject().delete(svc)));
            }
        } else {
            val svc = servicesManager.getObject().findServiceBy(
                configurationContext.getObject().getServiceFactory().createService(id));
            if (svc != null) {
                return ResponseEntity.ok(MAPPER.writeValueAsString(
                    servicesManager.getObject().delete(svc)));
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
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Import registered services as a JSON document or a zip file")
    public ResponseEntity importService(final HttpServletRequest request) throws Exception {
        val contentType = request.getContentType();
        if (Strings.CI.equalsAny(MediaType.APPLICATION_OCTET_STREAM_VALUE, contentType)) {
            return importServicesAsStream(request);
        }
        return importSingleService(request);
    }

    /**
     * Export registered services as a zip file: all of them, or only those whose ids are given.
     *
     * @param ids the ids of the registered services to export, or none to export every one of them
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export registered services as a zip file",
        parameters = @Parameter(name = "ids", description = "The ids of the registered services to export; all of them when left out",
            in = ParameterIn.QUERY))
    public ResponseEntity<Resource> export(@RequestParam(name = "ids", required = false) final Set<Long> ids) {
        val selected = ids == null || ids.isEmpty()
            ? null
            : ids.stream().map(id -> servicesManager.getObject().findServiceBy(id)).filter(Objects::nonNull).toList();
        if (selected != null && selected.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val resource = CompressionUtils.toZipFile(selected == null ? servicesManager.getObject().stream() : selected.stream(),
            Unchecked.function(entry -> {
                val service = (RegisteredService) entry;
                val fileName = String.format("%s-%s", service.getName(), service.getId());
                val sourceFile = Files.createTempFile(fileName, ".json").toFile();
                serializer.to(sourceFile, service);
                return sourceFile;
            }), "services");
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        headers.put("Filename", List.of("services.zip"));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Export response.
     *
     * @param id the id
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = "/export/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Export registered services as a single JSON file",
        parameters = @Parameter(name = "id", required = true, description = "The id of the registered service to export", in = ParameterIn.PATH))
    public ResponseEntity<Resource> export(@PathVariable final long id)
            throws Exception {
        val registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
        val registeredService = servicesManager.getObject().findServiceBy(id);
        if (registeredService == null) {
            return ResponseEntity.notFound().build();
        }
        val fileName = String.format("%s-%s", registeredService.getName(), registeredService.getId());
        val serviceFile = Files.createTempFile(fileName, ".json").toFile();
        registeredServiceSerializer.to(serviceFile, registeredService);
        val headers = new HttpHeaders();
        val resource = new TemporaryFileSystemResource(serviceFile);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        headers.put("Filename", List.of(serviceFile.getName()));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Save service response entity.
     *
     * @param registeredServiceBody the registered service body
     * @return the response entity
     */
    @PostMapping(consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @ResponseBody
    @Operation(summary = "Save registered service supplied in the request body",
        parameters = @Parameter(name = "body", required = true, description = "The request body to contain service definition"))
    public ResponseEntity saveService(
        @RequestBody final String registeredServiceBody) {
        val registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
        val registeredService = registeredServiceSerializer.from(registeredServiceBody);
        if (registeredService == null) {
            return unreadableService(registeredServiceSerializer, registeredServiceBody);
        }
        val incomplete = incompleteService(registeredService);
        if (incomplete != null) {
            return incomplete;
        }
        registeredService.setId(RandomUtils.nextInt());
        val result = servicesManager.getObject().save(registeredService);
        return ResponseEntity.ok(registeredServiceSerializer.toString(result));
    }

    /**
     * Validate service response entity.
     *
     * @param contentType           the content type
     * @param registeredServiceBody the registered service body
     * @return the response entity
     */
    @PostMapping(
        path = "/validate",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_YAML_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        }, produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_YAML_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @ResponseBody
    @Operation(summary = "Validate registered service supplied in the request body",
        parameters = @Parameter(name = "body", required = true, description = "The request body to contain service definition in JSON or YAML format"))
    public ResponseEntity validateService(
        @RequestHeader(HttpHeaders.CONTENT_TYPE) final String contentType,
        @RequestBody final String registeredServiceBody) {
        try {
            val isYaml = MediaType.APPLICATION_YAML.isCompatibleWith(MediaType.valueOf(contentType))
                || MediaType.parseMediaType(MEDIA_TYPE_CAS_YAML).isCompatibleWith(MediaType.valueOf(contentType));
            val registeredServiceSerializer = isYaml
                ? new RegisteredServiceYamlSerializer(applicationContext)
                : new RegisteredServiceJsonSerializer(applicationContext);
            val registeredService = registeredServiceSerializer.from(registeredServiceBody);
            if (registeredService == null) {
                return unreadableService(registeredServiceSerializer, registeredServiceBody);
            }
            val incomplete = incompleteService(registeredService);
            return incomplete != null ? incomplete : ResponseEntity.ok(true);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Update service response entity.
     *
     * @param registeredServiceBody the registered service body
     * @return the response entity
     */
    @PutMapping(consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @ResponseBody
    @Operation(summary = "Update registered service supplied in the request body",
        parameters = @Parameter(name = "body", required = true, description = "The request body to contain service definition"))
    public ResponseEntity updateService(
        @RequestBody final String registeredServiceBody) {
        val registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
        val registeredService = registeredServiceSerializer.from(registeredServiceBody);
        if (registeredService == null) {
            return unreadableService(registeredServiceSerializer, registeredServiceBody);
        }
        val incomplete = incompleteService(registeredService);
        if (incomplete != null) {
            return incomplete;
        }
        val result = servicesManager.getObject().save(registeredService);
        return ResponseEntity.ok(registeredServiceSerializer.toString(result));
    }

    /**
     * Refuse a definition that lacks what every registered service needs, a name and a service id, saying which is
     * missing, rather than failing while it is saved.
     *
     * @param registeredService the definition
     * @return a bad request naming what is missing, or null when nothing is
     */
    private static ResponseEntity<Map<String, String>> incompleteService(final RegisteredService registeredService) {
        val missing = new ArrayList<String>();
        if (StringUtils.isBlank(registeredService.getName())) {
            missing.add("name");
        }
        if (StringUtils.isBlank(registeredService.getServiceId())) {
            missing.add("serviceId");
        }
        if (missing.isEmpty()) {
            return null;
        }
        return ResponseEntity.badRequest().body(Map.of("error",
            "The definition has no %s".formatted(String.join(" and no ", missing))));
    }

    /**
     * Refuse a definition that cannot be read as a registered service, saying why. The serializer only logs the
     * reason, so the definition is read once more to report it.
     *
     * @param serializer the serializer that could not read the definition
     * @param body       the definition
     * @return a bad request carrying the reason
     */
    private static ResponseEntity<Map<String, String>> unreadableService(final RegisteredServiceJsonSerializer serializer,
                                                                         final String body) {
        var reason = "The definition cannot be read as a registered service";
        try {
            serializer.getObjectMapper().readValue(body, RegisteredService.class);
        } catch (final Exception e) {
            reason = StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName());
        }
        return ResponseEntity.badRequest().body(Map.of("error", reason));
    }

    private ResponseEntity<RegisteredService> importSingleService(final HttpServletRequest request) throws IOException {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Submitted registered service:\n[{}]", requestBody);

        if (StringUtils.isBlank(requestBody)) {
            LOGGER.warn("Could not extract registered services from request body");
            return ResponseEntity.badRequest().build();
        }

        return readRegisteredService(requestBody)
            .map(service -> {
                LOGGER.trace("Storing registered service:\n[{}]", service);
                return servicesManager.getObject().save(service);
            })
            .map(service -> {
                val headers = new HttpHeaders();
                headers.put("id", CollectionUtils.wrapList(String.valueOf(service.getId())));
                return new ResponseEntity<>(service, headers, HttpStatus.CREATED);
            })
            .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Read the JSON definitions of an archive, each once, and save them one by one, with the limits and the answer every
     * archive import has. A definition is read by the serializer that recognizes it, so it is neither parsed nor saved
     * twice when more than one serializer could read it; one that cannot be read, or that CAS does not save, is counted
     * as skipped and does not stop the rest.
     *
     * @param request the request carrying the archive
     * @return how many definitions were saved and how many were not
     * @throws IOException if the archive cannot be read
     */
    private ResponseEntity<Map<String, Integer>> importServicesAsStream(final HttpServletRequest request) throws IOException {
        return importArchive(request.getInputStream(),
            name -> !name.contains("..") && name.endsWith(".json"),
            content -> readRegisteredService(content).map(this::saveImportedService).orElse(Boolean.FALSE));
    }

    private boolean saveImportedService(final RegisteredService service) {
        try {
            return servicesManager.getObject().save(service) != null;
        } catch (final Exception e) {
            LOGGER.warn("Unable to save the imported registered service [{}]: [{}]", service.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Read one definition with the first serializer that recognizes its content, and only when none does, with the
     * others in turn. Serializers that recognize nothing do not get to log a failed parse for every definition, and one
     * that fails on the content counts as not reading it, so a broken definition does not stop the rest of an archive.
     *
     * @param content the definition
     * @return the registered service, if a serializer could read it
     */
    private Optional<RegisteredService> readRegisteredService(final String content) {
        val serializers = List.<StringSerializer<RegisteredService>>copyOf(registeredServiceSerializers.getObject());
        return Stream.concat(
                serializers.stream().filter(serializer -> serializer.supports(content)),
                serializers.stream().filter(serializer -> !serializer.supports(content)))
            .map(serializer -> readWith(serializer, content))
            .flatMap(Optional::stream)
            .findFirst();
    }

    private static Optional<RegisteredService> readWith(final StringSerializer<RegisteredService> serializer, final String content) {
        try {
            return Optional.ofNullable(serializer.from(content));
        } catch (final Exception e) {
            LOGGER.debug("Serializer [{}] cannot read the definition: [{}]", serializer.getClass().getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }
}
