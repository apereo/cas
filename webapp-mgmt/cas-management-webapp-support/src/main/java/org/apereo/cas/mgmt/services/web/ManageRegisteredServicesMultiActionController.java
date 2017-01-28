package org.apereo.cas.mgmt.services.web;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.mgmt.services.web.factory.RegisteredServiceFactory;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MultiActionController to handle the deletion of RegisteredServices as well as
 * displaying them on the Manage Services page.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller("manageRegisteredServicesMultiActionController")
public class ManageRegisteredServicesMultiActionController extends AbstractManagementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManageRegisteredServicesMultiActionController.class);
    
    private static final String STATUS = "status";

    private RegisteredServiceFactory registeredServiceFactory;

    private Service defaultService;

    /**
     * Instantiates a new manage registered services multi action controller.
     *
     * @param servicesManager              the services manager
     * @param registeredServiceFactory     the registered service factory
     * @param webApplicationServiceFactory the web application service factory
     * @param defaultServiceUrl            the default service url
     */
    public ManageRegisteredServicesMultiActionController(
            final ServicesManager servicesManager,
            final RegisteredServiceFactory registeredServiceFactory,
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final String defaultServiceUrl) {
        super(servicesManager);
        this.registeredServiceFactory = registeredServiceFactory;
        this.defaultService = webApplicationServiceFactory.createService(defaultServiceUrl);
    }

    /**
     * Ensure default service exists.
     */
    private void ensureDefaultServiceExists() {
        this.servicesManager.load();
        final Collection<RegisteredService> c = this.servicesManager.getAllServices();
        if (c == null) {
            throw new IllegalStateException("Services cannot be empty");
        }

        if (!this.servicesManager.matchesExistingService(this.defaultService)) {
            final RegexRegisteredService svc = new RegexRegisteredService();
            svc.setServiceId('^' + this.defaultService.getId());
            svc.setName("Services Management Web Application");
            svc.setDescription(svc.getName());
            this.servicesManager.save(svc);
            this.servicesManager.load();
        }
    }

    /**
     * Authorization failure handling. Simply returns the view name.
     *
     * @return the view name.
     */
    @GetMapping(value = "/authorizationFailure")
    public String authorizationFailureView() {
        return "authorizationFailure";
    }

    /**
     * Logout handling. Simply returns the view name.
     *
     * @param request the request
     * @param session the session
     * @return the view name.
     */
    @GetMapping(value = "/logout")
    public String logoutView(final HttpServletRequest request, final HttpSession session) {
        LOGGER.debug("Invalidating application session...");
        session.invalidate();
        return "logout";
    }

    /**
     * Method to delete the RegisteredService by its ID. Will make sure
     * the default service that is the management app itself cannot be deleted
     * or the user will be locked out.
     *
     * @param idAsLong the id
     * @param response the response
     */
    @PostMapping(value = "/deleteRegisteredService")
    public void deleteRegisteredService(@RequestParam("id") final long idAsLong,
                                        final HttpServletResponse response) {
        final RegisteredService svc = this.servicesManager.findServiceBy(this.defaultService);
        if (svc == null || svc.getId() == idAsLong) {
            throw new IllegalArgumentException("The default service " + this.defaultService.getId() + " cannot be deleted. "
                    + "The definition is required for accessing the application.");
        }

        final RegisteredService r = this.servicesManager.delete(idAsLong);
        if (r == null) {
            throw new IllegalArgumentException("Service id " + idAsLong + " cannot be found.");
        }
        final Map<String, Object> model = new HashMap<>();
        model.put("serviceName", r.getName());
        model.put(STATUS, HttpServletResponse.SC_OK);
        JsonUtils.render(model, response);
    }

    /**
     * Method to show the RegisteredServices.
     *
     * @param response the response
     * @return the Model and View to go to after the services are loaded.
     */
    @GetMapping(value = "/manage.html")
    public ModelAndView manage(final HttpServletResponse response) {
        ensureDefaultServiceExists();
        final Map<String, Object> model = new HashMap<>();
        model.put("defaultServiceUrl", this.defaultService.getId());
        model.put(STATUS, HttpServletResponse.SC_OK);
        return new ModelAndView("manage", model);
    }

    /**
     * Gets services.
     *
     * @param response the response
     */
    @GetMapping(value = "/getServices")
    public void getServices(final HttpServletResponse response) {
        ensureDefaultServiceExists();
        final Map<String, Object> model = new HashMap<>();
        final List<RegisteredServiceViewBean> serviceBeans = new ArrayList<>();
        final List<RegisteredService> services = new ArrayList<>(this.servicesManager.getAllServices());
        serviceBeans.addAll(services.stream().map(this.registeredServiceFactory::createServiceViewBean).collect(Collectors.toList()));
        model.put("services", serviceBeans);
        model.put(STATUS, HttpServletResponse.SC_OK);
        JsonUtils.render(model, response);
    }

    /**
     * Updates the {@link RegisteredService#getEvaluationOrder()}.
     *
     * @param response the response
     * @param id       the service ids, whose order also determines the service evaluation order
     */
    @PostMapping(value = "/updateRegisteredServiceEvaluationOrder")
    public void updateRegisteredServiceEvaluationOrder(final HttpServletResponse response,
                                                       @RequestParam("id") final long... id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("No service id was received. Re-examine the request");
        }
        for (int i = 0; i < id.length; i++) {
            final long svcId = id[i];
            final RegisteredService svc = this.servicesManager.findServiceBy(svcId);
            if (svc == null) {
                throw new IllegalArgumentException("Service id " + svcId + " cannot be found.");
            }
            svc.setEvaluationOrder(i);
            this.servicesManager.save(svc);
        }
        final Map<String, Object> model = new HashMap<>();
        model.put(STATUS, HttpServletResponse.SC_OK);
        JsonUtils.render(model, response);
    }


}
