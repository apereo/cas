package org.apereo.cas.support.openid.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Maps requests for usernames to a page that displays the Login URL for an
 * OpenId Identity Provider.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdProviderController extends AbstractController {
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        return new ModelAndView("openIdProviderView", "openid_server", casProperties.getServer().getPrefix());
    }
}
