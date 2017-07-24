package org.apereo.cas.mgmt.services.web;

import com.google.common.base.Throwables;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.mgmt.services.web.factory.RegisteredServiceFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

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
     * Instance of the RegisteredServiceFactory.
     */
    private final RegisteredServiceFactory registeredServiceFactory;

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager          the services manager
     * @param registeredServiceFactory the registered service factory
     */
    public RegisteredServiceSimpleFormController(final ServicesManager servicesManager, final RegisteredServiceFactory registeredServiceFactory) {
        super(servicesManager);
        this.registeredServiceFactory = registeredServiceFactory;
    }

    /**
     * Adds the service to the Service Registry.
     *
     * @param service  the edit bean
     */
    @PostMapping(value = "saveService", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveService(@RequestBody final RegisteredServiceEditBean.ServiceData service) {
        final RegisteredService svcToUse = this.registeredServiceFactory.createRegisteredService(service);
        final RegisteredService newSvc = this.servicesManager.save(svcToUse);
        LOGGER.info("Saved changes to service [{}]", svcToUse.getId());
        return new ResponseEntity<String>(String.valueOf(newSvc.getId()),HttpStatus.OK);
    }

    /**
     * Gets service by id.
     *
     * @param id       the id
     * @param request  the request
     * @param response the response
     */
    @GetMapping(value = "getService")
    public ResponseEntity<RegisteredServiceEditBean> getServiceById(@RequestParam(value = "id", required = false) final Long id,
                               final HttpServletRequest request, final HttpServletResponse response) {
        try {
            final RegisteredServiceEditBean bean = new RegisteredServiceEditBean();
            if (id == -1) {
                bean.setServiceData(null);
            } else {
                final RegisteredService service = this.servicesManager.findServiceBy(id);

                if (service == null) {
                    LOGGER.warn("Invalid service id specified [{}]. Cannot find service in the registry", id);
                    throw new IllegalArgumentException("Service id " + id + " cannot be found");
                }
                bean.setServiceData(this.registeredServiceFactory.createServiceData(service));
            }
            bean.setFormData(this.registeredServiceFactory.createFormData());

            return new ResponseEntity<RegisteredServiceEditBean>(bean,HttpStatus.OK);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
