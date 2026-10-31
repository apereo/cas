package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcCallbackAuthorizeViewResolver}. With {@code prompt=none}, the request is resumed only for the user of
 * the ticket that the callback has just validated, when that login satisfies the request's {@code prompt=login} and
 * {@code max_age}; a gateway login that returns no ticket means there is no single sign-on session, whatever profile the
 * session still holds. Otherwise the {@code login_required} error goes to the redirect URI of the request when the client
 * registered it. A request for a fresh
 * login ({@code prompt=login} or {@code max_age=0}) loses that request only when the callback has just validated a service
 * ticket that comes from a new login; any other visit to the callback, or a ticket issued from the existing single sign-on
 * session, resumes the request as it is, so the user is asked to log in again.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcCallbackAuthorizeViewResolver implements OAuth20CallbackAuthorizeViewResolver {
    private final ServicesManager servicesManager;

    private final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder;

    private final OAuth20RequestParameterResolver oauthRequestParameterResolver;

    @Override
    public ModelAndView resolve(final HttpServletRequest request, final HttpServletResponse response,
                                final @Nullable OAuth20UserProfile profile, final String url) {
        val prompt = oauthRequestParameterResolver.resolveSupportedPromptValues(url);
        if (prompt.contains(OidcConstants.PROMPT_NONE)) {
            val validatedProfile = request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE) != null ? profile : null;
            if (validatedProfile != null && !isFreshLoginRequired(prompt, url, validatedProfile)) {
                LOGGER.trace("Redirecting to URL [{}] without prompting for login", url);
                return OAuth20CallbackAuthorizeViewResolver.asDefault().resolve(request, response, validatedProfile, url);
            }
            val originalRedirectUrl = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.REDIRECT_URI);
            if (originalRedirectUrl.isEmpty()) {
                val model = new HashMap<String, String>();
                model.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
                return new ModelAndView(new JacksonJsonView(), model);
            }
            val parameters = new LinkedHashMap<String, String>();
            parameters.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
            oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.STATE)
                .ifPresent(state -> parameters.put(OAuth20Constants.STATE, state));
            val clientId = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);

            OAuth20Utils.validateRedirectUri(originalRedirectUrl.get(), true);
            val redirect = originalRedirectUrl.get();
            if (registeredService == null || !registeredService.matches(redirect)) {
                LOGGER.warn("Redirect URI [{}] is not registered for client [{}]; the error is not sent to it", redirect, clientId);
                val model = new HashMap<String, String>();
                model.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
                return new ModelAndView(new JacksonJsonView(), model);
            }
            val responseType = oauthRequestParameterResolver.resolveResponseModeType(request);
            return FunctionUtils.doUnchecked(() -> {
                LOGGER.warn("Unable to detect authenticated user profile for prompt-less login attempts. Redirecting to URL [{}]", redirect);
                return authorizationModelAndViewBuilder.build(registeredService, responseType, redirect, parameters);
            });
        }
        if (Boolean.TRUE.equals(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_NEW_LOGIN))
            && (prompt.contains(OidcConstants.PROMPT_LOGIN) || OidcRequestSupport.isZeroMaxAgeAuthorizationRequest(url))) {
            LOGGER.trace("Removing the request for a fresh login from URL [{}]", url);
            val newUrl = OidcRequestSupport.removeZeroMaxAgeFromAuthorizationRequest(
                OidcRequestSupport.removeOidcPromptFromAuthorizationRequest(url, OidcConstants.PROMPT_LOGIN));
            LOGGER.trace("Redirecting to URL [{}]", newUrl);
            return OAuth20CallbackAuthorizeViewResolver.asDefault().resolve(request, response, profile, newUrl);
        }
        LOGGER.trace("Redirecting to URL [{}]", url);
        return OAuth20CallbackAuthorizeViewResolver.asDefault().resolve(request, response, profile, url);
    }

    /**
     * Whether the authorization request asks for a login that the profile cannot satisfy: {@code prompt=login},
     * {@code max_age=0}, or a {@code max_age} that the authentication behind the profile exceeds. Under {@code prompt=none}
     * such a request is answered with {@code login_required}; resuming it would send the browser back to the login handshake
     * without end, since the gateway login only reuses the single sign-on session (OpenID Connect Core, section 3.1.2.6).
     *
     * @param prompt  the prompt values of the request
     * @param url     the url of the request
     * @param profile the profile
     * @return true or false
     */
    protected boolean isFreshLoginRequired(final Set<String> prompt, final String url, final OAuth20UserProfile profile) {
        return prompt.contains(OidcConstants.PROMPT_LOGIN)
            || OidcRequestSupport.isZeroMaxAgeAuthorizationRequest(url)
            || OidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(url, profile);
    }

}
