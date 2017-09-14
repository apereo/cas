package org.apereo.cas.mgmt.services.web;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.mgmt.services.web.factory.RegisteredServiceFactory;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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

    @Autowired
    private CasConfigurationProperties casProperties;

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
    @GetMapping(value = "/logout.html")
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
     * @return the response entity
     */
    @GetMapping(value = "/deleteRegisteredService")
    public ResponseEntity<String> deleteRegisteredService(@RequestParam("id") final long idAsLong,
                                                          final HttpServletResponse response) throws Exception {
        final RegisteredService svc = this.servicesManager.findServiceBy(this.defaultService);
        if (svc == null || svc.getId() == idAsLong) {
            return new ResponseEntity<String>("The default service " + this.defaultService.getId() + " cannot be deleted. "
                    + "The definition is required for accessing the application.", HttpStatus.BAD_REQUEST);
        }

        final RegisteredService r = this.servicesManager.delete(idAsLong);
        if (r == null) {
            return new ResponseEntity<String>("Service id " + idAsLong + " cannot be found.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>(r.getName(), HttpStatus.OK);
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
        model.put("type", this.casProperties.getServiceRegistry().getManagementType());
        model.put(STATUS, HttpServletResponse.SC_OK);
        return new ModelAndView("manage", model);
    }

    /**
     * Gets domains.
     *
     * @return the domains
     * @throws Exception the exception
     */
    @GetMapping(value = "/domains")
    public ResponseEntity<Collection<String>> getDomains() throws Exception {
        final Collection<String> data = this.servicesManager.getDomains();
        return new ResponseEntity<Collection<String>>(data, HttpStatus.OK);
    }

    /**
     * Gets services.
     *
     * @param domain the domain for which services will be retrieved
     * @return the services
     */
    @GetMapping(value = "/getServices")
    public ResponseEntity<List<RegisteredServiceViewBean>> getServices(@RequestParam final String domain) {
        ensureDefaultServiceExists();
        final List<RegisteredServiceViewBean> serviceBeans = new ArrayList<>();
        final List<RegisteredService> services = new ArrayList<>(this.servicesManager.getServicesForDomain(domain));
        serviceBeans.addAll(services.stream().map(this.registeredServiceFactory::createServiceViewBean).collect(Collectors.toList()));
        return new ResponseEntity<>(serviceBeans, HttpStatus.OK);
    }

    /**
     * Method will filter all services in the register using the passed string a regular expression against the
     * service name, service id, and service description.
     *
     * @param query - a string representing text to search for
     * @return - the resulting services
     */
    @GetMapping(value = "/search")
    public ResponseEntity<List<RegisteredServiceViewBean>> search(@RequestParam final String query) {
        final Pattern pattern = RegexUtils.createPattern("^.*" + query + ".*$");
        final List<RegisteredServiceViewBean> serviceBeans = new ArrayList<>();
        final List<RegisteredService> services = this.servicesManager.getAllServices()
                .stream()
                .filter(service -> pattern.matcher(service.getServiceId()).lookingAt()
                        || pattern.matcher(service.getName()).lookingAt()
                        || pattern.matcher(service.getDescription()).lookingAt())
                .collect(Collectors.toList());
        serviceBeans.addAll(services.stream().map(this.registeredServiceFactory::createServiceViewBean).collect(Collectors.toList()));
        return new ResponseEntity<>(serviceBeans, HttpStatus.OK);
    }

    /**
     * Method will update the order of two services passed in.
     *
     * @param request  the request
     * @param response the response
     * @param svcs     the services to be updated
     */
    @PostMapping(value = "/updateOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateOrder(final HttpServletRequest request, final HttpServletResponse response,
                            @RequestBody final RegisteredServiceViewBean[] svcs) {
        final String id = svcs[0].getAssignedId();
        final RegisteredService svcA = this.servicesManager.findServiceBy(Long.parseLong(id));
        if (svcA == null) {
            throw new IllegalArgumentException("Service " + id + " cannot be found");
        }
        final String id2 = svcs[1].getAssignedId();
        final RegisteredService svcB = this.servicesManager.findServiceBy(Long.parseLong(id2));
        if (svcB == null) {
            throw new IllegalArgumentException("Service " + id2 + " cannot be found");
        }
        svcA.setEvaluationOrder(svcs[0].getEvalOrder());
        svcB.setEvaluationOrder(svcs[1].getEvalOrder());
        this.servicesManager.save(svcA);
        this.servicesManager.save(svcB);
    }
}

