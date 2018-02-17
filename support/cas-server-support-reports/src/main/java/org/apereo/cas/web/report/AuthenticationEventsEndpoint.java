package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link AuthenticationEventsEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Endpoint(id = "authenticationEvents")
public class AuthenticationEventsEndpoint extends BaseCasMvcEndpoint {

    private final CasEventRepository eventRepository;

    public AuthenticationEventsEndpoint(final CasEventRepository eventRepository, final CasConfigurationProperties casProperties) {
        super(casProperties.getMonitor().getEndpoints().getAuthenticationEvents(), casProperties);
        this.eventRepository = eventRepository;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        return new ModelAndView("monitoring/viewAuthenticationEvents");
    }

    /**
     * Gets records.
     *
     * @param request  the request
     * @param response the response
     * @return the records
     */
    @GetMapping(value = "/getEvents")
    @ResponseBody
    @ReadOperation
    public Collection<? extends CasEvent> getRecords(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        return this.eventRepository.load();
    }
}
