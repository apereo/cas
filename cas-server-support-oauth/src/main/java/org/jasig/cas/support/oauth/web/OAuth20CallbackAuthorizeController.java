package org.jasig.cas.support.oauth.web;

import org.pac4j.core.config.Config;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.springframework.web.CallbackController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
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
@Component("callbackAuthorizeController")
public class OAuth20CallbackAuthorizeController extends AbstractController {

    @Autowired(required = false)
    @Qualifier("oauthSecConfig")
    private Config config;

    @Autowired(required = false)
    private CallbackController callbackController;

    @PostConstruct
    private void postConstruct() {
        if (callbackController != null) {
            callbackController.setConfig(config);
        }
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        CommonHelper.assertNotNull("callbackController", callbackController);

        return new ModelAndView(callbackController.callback(request, response));
    }
}
