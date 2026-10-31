package org.apereo.cas.support.oauth.web.response.callback;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

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

    private final OAuth20RequestParameterResolver requestParameterResolver;

    private final OAuth20ResponseModeFactory responseModeFactory;


    /**
     * Build string.
     *
     * @param request the request
     * @return the view response
     */
    public ModelAndView build(final HttpServletRequest request) throws Exception {
        val errorWithCallBack = Boolean.TRUE.equals(request.getAttribute(OAuth20Constants.ERROR_WITH_CALLBACK));
        if (!errorWithCallBack) {
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        val error = Optional.ofNullable(request.getAttribute(OAuth20Constants.ERROR))
            .orElseThrow()
            .toString();
        val errorDescription = Optional.ofNullable(request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION))
            .orElse(StringUtils.EMPTY)
            .toString();
        val clientId = Optional.ofNullable(request.getParameter(OAuth20Constants.CLIENT_ID))
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        val redirectUri = Optional.ofNullable(request.getParameter(OAuth20Constants.REDIRECT_URI))
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);

        val state = Optional.ofNullable(request.getParameter(OAuth20Constants.STATE))
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
        return buildResponseModelAndView(request, servicesManager, clientId, redirectUri, params);
    }

    /**
     * Build response model and view.
     *
     * @param request         the request
     * @param servicesManager the services manager
     * @param clientId        the client id
     * @param redirectUrl     the redirect url
     * @param parameters      the parameters
     * @return the model and view
     */
    public ModelAndView buildResponseModelAndView(final HttpServletRequest request,
                                                  final ServicesManager servicesManager,
                                                  final String clientId,
                                                  final String redirectUrl,
                                                  final Map<String, String> parameters)
            throws Exception {
        OAuth20Utils.validateRedirectUri(redirectUrl, true);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        val responseType = requestParameterResolver.resolveResponseModeType(request);

        val builder = responseModeFactory.getBuilder(Objects.requireNonNull(registeredService), responseType);
        return builder.build(registeredService, redirectUrl, parameters);
    }

    /**
     * Determines whether this builder supports the request.
     *
     * @param request the request
     * @return true/false
     */
    public boolean supports(final HttpServletRequest request) {
        return request.getAttribute(OAuth20Constants.ERROR) != null;
    }
}
