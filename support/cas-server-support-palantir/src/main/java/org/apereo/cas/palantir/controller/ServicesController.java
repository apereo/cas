package org.apereo.cas.palantir.controller;

import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
