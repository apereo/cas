package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServicesReportController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RegisteredServicesReportController extends BaseCasMvcEndpoint {
    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;

    /**
     * Instantiates a new mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param casProperties   the cas properties
     * @param servicesManager the services manager
     */
    public RegisteredServicesReportController(final CasConfigurationProperties casProperties,
                                              final ServicesManager servicesManager) {
        super("casservices", "/services",
                casProperties.getMonitor().getEndpoints().getRegisteredServicesReport(), casProperties);
        this.servicesManager = servicesManager;
        this.casProperties = casProperties;
    }

    /**
     * Handle and produce a list of services from registry.
     *
     * @param request  the request
     * @param response the response
     * @return the web async task
     */
    @GetMapping
    @ResponseBody
    public WebAsyncTask<Map<String, Object>> handle(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        final Callable<Map<String, Object>> asyncTask = () -> this.servicesManager.getAllServices()
                .stream()
                .collect(Collectors.toMap(RegisteredService::getName, Function.identity()));
        return new WebAsyncTask<>(casProperties.getHttpClient().getAsyncTimeout(), asyncTask);
    }
}
