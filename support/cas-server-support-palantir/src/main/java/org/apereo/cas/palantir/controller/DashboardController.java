package org.apereo.cas.palantir.controller;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.ServicesManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
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
@RequiredArgsConstructor
public class DashboardController {
    private final CasConfigurationProperties casProperties;
    private final ObjectProvider<ServicesManager> servicesManager;
    
    /**
     * Dashboard root.
     *
     * @return the model and view
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView dashboardHome(final Authentication authentication) {
        return buildModelAndView(authentication);
    }

    /**
     * Dashboard home page explicitly defined.
     *
     * @return the model and view
     */
    @GetMapping(path = {"/dashboard", "/", "/dashboard/**"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView dashboardRoot(final Authentication authentication) {
        return buildModelAndView(authentication);
    }

    private ModelAndView buildModelAndView(final Authentication authentication) {
        val mav = new ModelAndView("palantir/casPalantirDashboardView");
        mav.addObject("authentication", authentication);
        mav.addObject("casServerPrefix", casProperties.getServer().getPrefix());
        mav.addObject("authorizedServices", servicesManager.getObject().getAllServices());
        return mav;
    }
}
