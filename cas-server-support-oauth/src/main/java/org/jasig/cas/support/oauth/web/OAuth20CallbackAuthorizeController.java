package org.jasig.cas.support.oauth.web;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.CallbackController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuth callback authorize controller based on the pac4j callback controller.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@RefreshScope
@Controller("callbackAuthorizeController")
public class OAuth20CallbackAuthorizeController extends AbstractController {
    
    @Autowired
    @Qualifier("oauthSecConfig")
    private Config config;
    
    @Autowired
    private CallbackController callbackController;

    @PostConstruct
    private void postConstruct() {
        this.callbackController.setConfig(this.config);
    }

    @RequestMapping(path= OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.CALLBACK_AUTHORIZE_URL)
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView(this.callbackController.callback(request, response));
    }
}
