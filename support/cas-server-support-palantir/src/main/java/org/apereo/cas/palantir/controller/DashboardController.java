package org.apereo.cas.palantir.controller;

import org.apereo.cas.palantir.PalantirConstants;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link DashboardController}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Controller
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR)
public class DashboardController {
    private static final ModelAndView VIEW_DASHBOARD = new ModelAndView("palantir/dashboard");

    /**
     * Dashboard root.
     *
     * @return the model and view
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView dashboardRoot() {
        return VIEW_DASHBOARD;
    }

    /**
     * Dashboard home page explicitly defined.
     *
     * @return the model and view
     */
    @GetMapping(path = {"/dashboard", "/", "/dashboard/**"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView dashboardHome() {
        return VIEW_DASHBOARD;
    }
}
