package org.jasig.cas.support.openid.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
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
@RefreshScope
@Component("openIdProviderController")
public class OpenIdProviderController extends AbstractController {

    
    @Value("${server.prefix}")
    private String serverPrefix;

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        return new ModelAndView("openIdProviderView", "openid_server", this.serverPrefix);
    }

    public void setServerPrefix(final String serverPrefix) {
        this.serverPrefix = serverPrefix;
    }
}
