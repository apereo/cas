package org.jasig.cas.web;

import org.jasig.cas.OidcConstants;
import org.jasig.cas.support.oauth.web.OAuth20AuthorizeController;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcAuthorizeController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Controller("oidcAuthorizeController")
public class OidcAuthorizeController extends OAuth20AuthorizeController {
    
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/authorize", method = RequestMethod.GET)
    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
