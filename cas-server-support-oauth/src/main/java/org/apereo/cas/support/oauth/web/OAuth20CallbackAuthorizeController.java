package org.apereo.cas.support.oauth.web;

import org.apache.commons.lang.StringUtils;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.springframework.web.CallbackController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Controller("callbackAuthorizeController")
public class OAuth20CallbackAuthorizeController extends AbstractController {
    
    @Autowired
    @Qualifier("oauthSecConfig")
    private Config config;
    
    @Autowired
    private CallbackController callbackController;

    @Autowired
    @Qualifier("callbackAuthorizeViewResolver")
    private OAuth20CallbackAuthorizeViewResolver oAuth20CallbackAuthorizeViewResolver;
    
    @PostConstruct
    private void postConstruct() {
        this.callbackController.setConfig(this.config);
    }

    @RequestMapping(path= OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.CALLBACK_AUTHORIZE_URL)
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String url = StringUtils.remove(this.callbackController.callback(request, response), "redirect:");
        final J2EContext ctx = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(ctx);
        return oAuth20CallbackAuthorizeViewResolver.resolve(ctx, manager, url);
    }
}
