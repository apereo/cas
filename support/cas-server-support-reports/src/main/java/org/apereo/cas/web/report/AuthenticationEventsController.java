package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link AuthenticationEventsController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConditionalOnClass(value = CasEventRepository.class)
public class AuthenticationEventsController extends BaseCasMvcEndpoint {

    private final CasEventRepository eventRepository;

    public AuthenticationEventsController(final CasEventRepository eventRepository, final CasConfigurationProperties casProperties) {
        super("casauthnevents", "/authnEvents", casProperties.getMonitor().getEndpoints().getAuthenticationEvents(), casProperties);
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
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) {
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
    public Collection<? extends CasEvent> getRecords(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        return this.eventRepository.load();
    }
}
