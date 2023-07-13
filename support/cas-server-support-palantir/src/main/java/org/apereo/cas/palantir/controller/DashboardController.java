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

    /**
     * Dashboard home.
     *
     * @return the model and view
     */
    @GetMapping (path = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView dashboardHome() {
        return new ModelAndView("palantir/dashboard");
    }
}
