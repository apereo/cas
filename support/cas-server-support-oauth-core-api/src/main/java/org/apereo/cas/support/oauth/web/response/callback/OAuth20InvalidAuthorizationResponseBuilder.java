package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20InvalidAuthorizationResponseBuilder}.
 *
 * @author Julien Huon
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20InvalidAuthorizationResponseBuilder {
    private final ServicesManager servicesManager;

    /**
     * Build string.
     *
     * @param context  the context
     * @return the view response
     */
    public ModelAndView build(final JEEContext context) {
        val errorWithCallBack = (Boolean) context.getRequestAttribute(OAuth20Constants.ERROR_WITH_CALLBACK)
            .orElse(false);

        if (!errorWithCallBack) {
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        val error = context.getRequestAttribute(OAuth20Constants.ERROR)
            .get()
            .toString();
        val errorDescription = context.getRequestAttribute(OAuth20Constants.ERROR_DESCRIPTION)
            .orElse(StringUtils.EMPTY)
            .toString();
        val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        val redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);

        val state = context.getRequestParameter(OAuth20Constants.STATE)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);

        val params = new LinkedHashMap<String, String>();
        params.put(OAuth20Constants.ERROR, error);
        if (StringUtils.isNotBlank(errorDescription)) {
            params.put(OAuth20Constants.ERROR_DESCRIPTION, errorDescription);
        }
        if (StringUtils.isNotBlank(state)) {
            params.put(OAuth20Constants.STATE, state);
        }

        LOGGER.debug("Redirecting to URL [{}] with params [{}] for clientId [{}]", redirectUri, params.keySet(), clientId);
        return buildResponseModelAndView(context, servicesManager, clientId, redirectUri, params);
    }

    /**
     * Build response model and view.
     *
     * @param context         the context
     * @param servicesManager the services manager
     * @param clientId        the client id
     * @param redirectUrl     the redirect url
     * @param parameters      the parameters
     * @return the model and view
     */
    public ModelAndView buildResponseModelAndView(final JEEContext context, final ServicesManager servicesManager,
                                                  final String clientId, final String redirectUrl,
                                                  final Map<String, String> parameters) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        val responseType = OAuth20Utils.getResponseModeType(context);
        if (OAuth20Utils.isResponseModeTypeFormPost(registeredService, responseType)) {
            val model = new LinkedHashMap<String, Object>();
            model.put("originalUrl", redirectUrl);
            model.put("parameters", parameters);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_POST_RESPONSE, model);
        }
        return new ModelAndView(new RedirectView(redirectUrl), parameters);
    }

    /**
     * Supports request?
     *
     * @param context the context
     * @return true/false
     */
    public boolean supports(final JEEContext context) {
        return context.getRequestAttribute(OAuth20Constants.ERROR).isPresent();
    }
}
