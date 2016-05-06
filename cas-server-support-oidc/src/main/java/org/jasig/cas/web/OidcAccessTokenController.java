package org.jasig.cas.web;

import org.jasig.cas.OidcConstants;
import org.apereo.cas.support.oauth.web.AccessTokenResponseGenerator;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.web.OAuth20AccessTokenController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcAccessTokenController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Controller("oidcAccessTokenController")
public class OidcAccessTokenController extends OAuth20AccessTokenController {

    @Autowired
    @Qualifier("oidcAccessTokenResponseGenerator")
    private AccessTokenResponseGenerator accessTokenResponseGenerator;

    /**
     * Initi the response generator and the controller in general.
     */
    @PostConstruct
    protected void init() {
        setAccessTokenResponseGenerator(this.accessTokenResponseGenerator);
    }
    
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL, method = RequestMethod.POST)
    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
    
    
}
