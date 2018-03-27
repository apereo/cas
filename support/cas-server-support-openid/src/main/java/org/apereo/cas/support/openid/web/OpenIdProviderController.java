package org.apereo.cas.support.openid.web;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps requests for usernames to a page that displays the Login URL for an
 * OpenId Identity Provider.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller("openIdProviderController")
@Slf4j
public class OpenIdProviderController {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping("/openid/*")
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) {
        final Map model = new HashMap<>();
        model.put("openid_server", casProperties.getServer().getPrefix());
        return new ModelAndView("openIdProviderView", model);
    }
}
