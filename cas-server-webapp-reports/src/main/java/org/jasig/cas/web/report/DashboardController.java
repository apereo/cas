package org.jasig.cas.web.report;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link DashboardController}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Controller("dashboardController")
public class DashboardController {

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping("/status/dashboard")
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        return new ModelAndView("monitoring/viewDashboard");
    }
}
