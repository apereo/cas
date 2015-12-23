package org.jasig.cas.services.web;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean;
import org.jasig.cas.services.web.factory.RegisteredServiceFactory;
import org.jasig.cas.services.web.view.JsonViewUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle adding/editing of RegisteredServices.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller("registeredServiceSimpleFormController")
public final class RegisteredServiceSimpleFormController extends AbstractManagementController {

    /**
     * Instance of the RegisteredServiceFactory.
     */
    @NotNull
    private final RegisteredServiceFactory registeredServiceFactory;

    /**
     * Instantiates a new registered service simple form controller.
     *
     * @param servicesManager          the services manager
     * @param registeredServiceFactory the registered service factory
     */
    @Autowired
    public RegisteredServiceSimpleFormController(
        @Qualifier("servicesManager")
        final ReloadableServicesManager servicesManager,
        @Qualifier("registeredServiceFactory")
        final RegisteredServiceFactory registeredServiceFactory) {
        super(servicesManager);
        this.registeredServiceFactory = registeredServiceFactory;
    }

    /**
     * Adds the service to the Service Registry.
     * @param request the request
     * @param response the response
     * @param result the result
     * @param service the edit bean
     */
    @RequestMapping(method = RequestMethod.POST, value = {"saveService.html"})
    public void saveService(final HttpServletRequest request,
                            final HttpServletResponse response,
                            @RequestBody final RegisteredServiceEditBean.ServiceData service,
                            final BindingResult result) {
        try {

            final RegisteredService svcToUse = registeredServiceFactory.createRegisteredService(service);
            final RegisteredService newSvc = this.servicesManager.save(svcToUse);
            logger.info("Saved changes to service {}", svcToUse.getId());

            final Map<String, Object> model = new HashMap<>();
            model.put("id", newSvc.getId());
            model.put("status", HttpServletResponse.SC_OK);
            JsonViewUtils.render(model, response);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets service by id.
     *
     * @param id       the id
     * @param request  the request
     * @param response the response
     */
    @RequestMapping(method = RequestMethod.GET, value = {"getService.html"})
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
                bean.setServiceData(registeredServiceFactory.createServiceData(service));
            }
            bean.setFormData(registeredServiceFactory.createFormData());

            bean.setStatus(HttpServletResponse.SC_OK);
            JsonViewUtils.render(bean, response);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
