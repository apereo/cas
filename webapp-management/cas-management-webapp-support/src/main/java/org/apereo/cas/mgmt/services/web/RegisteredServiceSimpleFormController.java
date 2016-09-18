package org.apereo.cas.mgmt.services.web;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.mgmt.services.web.factory.RegisteredServiceFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.JsonUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

    /**
     * Instance of the RegisteredServiceFactory.
     */

    private RegisteredServiceFactory registeredServiceFactory;

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager          the services manager
     * @param registeredServiceFactory the registered service factory
     */
    public RegisteredServiceSimpleFormController(
            final ServicesManager servicesManager,
            final RegisteredServiceFactory registeredServiceFactory) {
        super(servicesManager);
        this.registeredServiceFactory = registeredServiceFactory;
    }

    /**
     * Adds the service to the Service Registry.
     *
     * @param request  the request
     * @param response the response
     * @param result   the result
     * @param service  the edit bean
     */
    @RequestMapping(method = RequestMethod.POST, value = {"saveService.html"})
    public void saveService(final HttpServletRequest request,
                            final HttpServletResponse response,
                            @RequestBody final RegisteredServiceEditBean.ServiceData service,
                            final BindingResult result) {
        try {
            if (StringUtils.isNotBlank(service.getAssignedId())) {
                final RegisteredService svc = this.servicesManager.findServiceBy(Long.parseLong(service.getAssignedId()));
                if (svc != null) {
                    this.servicesManager.delete(svc.getId());
                }
            }
            
            final RegisteredService svcToUse = this.registeredServiceFactory.createRegisteredService(service);
            final RegisteredService newSvc = this.servicesManager.save(svcToUse);
            logger.info("Saved changes to service {}", svcToUse.getId());

            final Map<String, Object> model = new HashMap<>();
            model.put("id", newSvc.getId());
            model.put("status", HttpServletResponse.SC_OK);
            JsonUtils.render(model, response);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Gets service by id.
     *
     * @param id       the id
     * @param request  the request
     * @param response the response
     */
    @RequestMapping(method = RequestMethod.GET, value = {"getService"})
    public void getServiceById(@RequestParam(value = "id", required = false) final Long id,
                               final HttpServletRequest request, final HttpServletResponse response) {

        try {
            final RegisteredServiceEditBean bean = new RegisteredServiceEditBean();
            if (id == -1) {
                bean.setServiceData(null);
            } else {
                final RegisteredService service = this.servicesManager.findServiceBy(id);

                if (service == null) {
                    logger.warn("Invalid service id specified [{}]. Cannot find service in the registry", id);
                    throw new IllegalArgumentException("Service id " + id + " cannot be found");
                }
                bean.setServiceData(this.registeredServiceFactory.createServiceData(service));
            }
            bean.setFormData(this.registeredServiceFactory.createFormData());

            bean.setStatus(HttpServletResponse.SC_OK);
            JsonUtils.render(bean, response);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
