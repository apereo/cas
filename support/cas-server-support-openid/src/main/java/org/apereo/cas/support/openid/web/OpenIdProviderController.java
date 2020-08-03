package org.apereo.cas.support.openid.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Maps requests for usernames to a page that displays the Login URL for an
 * OpenId Identity Provider.
 *
 * @author Scott Battaglia
 * @deprecated 6.2
 * @since 3.1
 */
@Controller("openIdProviderController")
@Deprecated(since = "6.2.0")
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
    public ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) {
        return new ModelAndView("openIdProviderView",
            CollectionUtils.wrap("openid_server", casProperties.getServer().getPrefix()));
    }
}
