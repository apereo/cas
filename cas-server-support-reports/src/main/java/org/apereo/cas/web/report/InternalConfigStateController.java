package org.apereo.cas.web.report;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that exposes the CAS internal state and beans
 * as JSON. The report is available at {@code /status/config}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Controller("internalConfigController")
public class InternalConfigStateController {

    private static final String VIEW_CONFIG = "monitoring/viewConfig";

    /**
     * Handle request.
     *
     * @param request the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.GET, value="/status/config")
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        return new ModelAndView(VIEW_CONFIG);
    }
    
}
