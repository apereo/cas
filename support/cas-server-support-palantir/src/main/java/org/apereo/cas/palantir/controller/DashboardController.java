package org.apereo.cas.palantir.controller;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.palantir.PalantirConstants;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * Dashboard home page explicitly defined.
     *
     * @return the model and view
     */
    @GetMapping(path = {StringUtils.EMPTY, "/dashboard", "/", "/dashboard/**"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView dashboardRoot(final Authentication authentication,
                                      final HttpServletRequest request) {
        return buildModelAndView(authentication, request);
    }

    private ModelAndView buildModelAndView(final Authentication authentication,
                                           final HttpServletRequest request) {
        val mav = new ModelAndView("palantir/casPalantirDashboardView");
        mav.addObject("authentication", authentication);
        mav.addObject("casServerPrefix", casProperties.getServer().getPrefix());
        mav.addObject("httpRequestSecure", request.isSecure());
        mav.addObject("httpRequestMethod", request.getMethod());
        return mav;
    }
}
