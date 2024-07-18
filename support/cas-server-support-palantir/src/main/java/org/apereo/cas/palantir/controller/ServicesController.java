package org.apereo.cas.palantir.controller;

import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.io.TemporaryFileSystemResource;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link ServicesController}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RestController
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR + "/services")
@RequiredArgsConstructor
public class ServicesController {
    private final ObjectProvider<ServicesManager> servicesManager;
    private final StringSerializer<RegisteredService> registeredServiceSerializer;

    /**
     * Gets all services.
     *
     * @return the all services
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllServices() {
        return ResponseEntity.ok(registeredServiceSerializer.fromList(servicesManager.getObject().getAllServices()));
    }

    /**
     * Gets service by numeric id.
     *
     * @param id the id
     * @return the service by numeric id
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getServiceByNumericId(@PathVariable("id") final long id) {
        val registeredService = servicesManager.getObject().findServiceBy(id);
        return registeredService != null
            ? ResponseEntity.ok(registeredServiceSerializer.toString(registeredService))
            : ResponseEntity.notFound().build();
    }

    /**
     * Delete service by numeric id response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteServiceByNumericId(@PathVariable("id") final long id) {
        return ResponseEntity.ok(registeredServiceSerializer.toString(servicesManager.getObject().delete(id)));
    }

    /**
     * Save service response entity.
     *
     * @param registeredServiceBody the registered service body
     * @return the response entity
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity saveService(@RequestBody final String registeredServiceBody) {
        val registeredService = registeredServiceSerializer.from(registeredServiceBody);
        registeredService.setId(RandomUtils.nextLong());
        val result = servicesManager.getObject().save(registeredService);
        return ResponseEntity.ok(registeredServiceSerializer.toString(result));
    }

    /**
     * Update service response entity.
     *
     * @param registeredServiceBody the registered service body
     * @return the response entity
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateService(@RequestBody final String registeredServiceBody) {
        val registeredService = registeredServiceSerializer.from(registeredServiceBody);
        val result = servicesManager.getObject().save(registeredService);
        return ResponseEntity.ok(registeredServiceSerializer.toString(result));
    }

    /**
     * Export single service by its id.
     *
     * @param id the id
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = "/export/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> export(@PathVariable("id") final long id) throws Exception {
        val registeredService = servicesManager.getObject().findServiceBy(id);
        val fileName = String.format("%s-%s", registeredService.getName(), registeredService.getId());
        val serviceFile = File.createTempFile(fileName, ".json");
        registeredServiceSerializer.to(serviceFile, registeredService);
        val headers = new HttpHeaders();
        val resource = new TemporaryFileSystemResource(serviceFile);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        headers.put("Filename", List.of(serviceFile.getName()));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Export services.
     *
     * @return the response entity
     */
    @GetMapping(path = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> export() {
        val resource = CompressionUtils.toZipFile(servicesManager.getObject().stream(),
            Unchecked.function(entry -> {
                val service = (RegisteredService) entry;
                val fileName = String.format("%s-%s", service.getName(), service.getId());
                val sourceFile = File.createTempFile(fileName, ".json");
                registeredServiceSerializer.to(sourceFile, service);
                return sourceFile;
            }), "services");
        val headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(Objects.requireNonNull(resource.getFilename())).build());
        headers.put("Filename", List.of("services.zip"));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
