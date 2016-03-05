package org.jasig.cas.support.openid.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * Maps requests for usernames to a page that displays the Login URL for an
 * OpenId Identity Provider.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("openIdProviderController")
public final class OpenIdProviderController extends AbstractController {

    @NotNull
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
