package org.apereo.cas.web.report;

import com.google.common.collect.Sets;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@Controller("authenticationEventsController")
@RequestMapping("/status/authnEvents")
public class AuthenticationEventsController {

    @Autowired
    private CasConfigurationProperties casProperties;

    private CasEventRepository eventRepository;

    public AuthenticationEventsController() {
    }

    public AuthenticationEventsController(final CasEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        return new ModelAndView("monitoring/viewAuthenticationEvents");
    }

    /**
     * Gets records.
     *
     * @param request  the request
     * @param response the response
     * @return the records
     * @throws Exception the exception
     */
    @RequestMapping(value = "/getEvents", method = RequestMethod.GET)
    @ResponseBody
    public Collection<CasEvent> getRecords(final HttpServletRequest request,
                                           final HttpServletResponse response)
            throws Exception {
        if (this.eventRepository != null) {
            return this.eventRepository.load();
        }
        return Sets.newHashSet();
    }

}
