package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
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
@Slf4j
public class RegisteredServicesReportController extends BaseCasMvcEndpoint {
    private final ServicesManager servicesManager;

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
    public WebAsyncTask<Map<Long, Object>> handle(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        final Callable<Map<Long, Object>> asyncTask = () -> this.servicesManager.load()
                .stream()
                .collect(Collectors.toMap(RegisteredService::getId, Function.identity()));
        final long timeout = Beans.newDuration(casProperties.getHttpClient().getAsyncTimeout()).toMillis();
        return new WebAsyncTask<>(timeout, asyncTask);
    }
}
