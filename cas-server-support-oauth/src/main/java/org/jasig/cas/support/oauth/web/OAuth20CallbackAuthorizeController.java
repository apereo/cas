package org.jasig.cas.support.oauth.web;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.CallbackController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * OAuth callback authorize controller based on the pac4j callback controller.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("callbackAuthorizeController")
public class OAuth20CallbackAuthorizeController extends AbstractController {

    @NotNull
    @Autowired
    @Qualifier("oauthSecConfig")
    private Config config;

    @NotNull
    @Autowired
    private CallbackController callbackController;

    @PostConstruct
    private void postConstruct() {
        callbackController.setConfig(config);
    }

    @RequestMapping(path= OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.CALLBACK_AUTHORIZE_URL)
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView(callbackController.callback(request, response));
    }
}
