package org.apereo.cas.mgmt.services.web;

import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handle adding/editing of RegisteredServices.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller("registeredServiceSimpleFormController")
public class RegisteredServiceSimpleFormController extends AbstractManagementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceSimpleFormController.class);

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager          the services manager
     */
    public RegisteredServiceSimpleFormController(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    /**
     * Adds the service to the Service Registry.
     *
     * @param service the edit bean
     * @return the response entity
     */
    @PostMapping(value = "saveService", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveService(@RequestBody final RegisteredService service) {
        final RegisteredService newSvc = this.servicesManager.save(service);
        LOGGER.info("Saved changes to service [{}]", service.getId());
        return new ResponseEntity<>(String.valueOf(newSvc.getId()), HttpStatus.OK);
    }

    /**
     * Gets service by id.
     *
     * @param id the id
     * @return the service by id
     */
    @GetMapping(value = "getService")
    public ResponseEntity<RegisteredService> getServiceById(@RequestParam(value = "id", required = false) final Long id) {
        final RegisteredService service;
        if (id == -1) {
            service = new RegexRegisteredService();
        } else {
            service = this.servicesManager.findServiceBy(id);
            if (service == null) {
                LOGGER.warn("Invalid service id specified [{}]. Cannot find service in the registry", id);
                throw new IllegalArgumentException("Service id " + id + " cannot be found");
            }
        }
        return new ResponseEntity<>(service, HttpStatus.OK);
    }
}
