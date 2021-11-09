package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DefaultOAuth20AuthorizationModelAndViewBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class DefaultOAuth20AuthorizationModelAndViewBuilder implements OAuth20AuthorizationModelAndViewBuilder {
    @Override
    public ModelAndView build(final WebContext context, final OAuthRegisteredService registeredService,
                              final String url, final Map<String, String> parameters) {
        val responseType = OAuth20Utils.getResponseModeType(context);
        val redirectUrl = prepareRedirectUrl(context, registeredService, url, parameters);
        if (OAuth20Utils.isResponseModeTypeFormPost(registeredService, responseType)) {
            val model = new LinkedHashMap<String, Object>();
            model.put("originalUrl", redirectUrl);
            model.put("parameters", parameters);
            val mv = new ModelAndView(CasWebflowConstants.VIEW_ID_POST_RESPONSE, model);
            LOGGER.debug("Redirecting to [{}] with model [{}]", mv.getViewName(), mv.getModel());
            return mv;
        }
        return new ModelAndView(new RedirectView(redirectUrl), parameters);
    }

    /**
     * Prepare.
     *
     * @param context           the context
     * @param registeredService the registered service
     * @param redirectUrl       the redirect url
     * @param parameters        the parameters
     * @return the string
     */
    protected String prepareRedirectUrl(final WebContext context, final OAuthRegisteredService registeredService,
                                        final String redirectUrl, final Map<String, String> parameters) {
        return redirectUrl;
    }
}
