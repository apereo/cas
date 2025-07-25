package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link BaseOAuth20AuthorizationRequestValidator}.
 *
 * @author Julien Huon
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class BaseOAuth20AuthorizationRequestValidator implements OAuth20AuthorizationRequestValidator {
    /**
     * Service manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    /**
     * Service access enforcer.
     */
    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    /**
     * Request parameter resolver.
     */
    protected final OAuth20RequestParameterResolver requestParameterResolver;

    protected boolean preValidate(final WebContext context) throws Throwable {
        val clientId = getClientIdFromRequest(context);
        val registeredService = verifyRegisteredServiceByClientId(context, clientId);
        if (registeredService == null) {
            return false;
        }

        val redirectUri = getRedirectUriFromRequest(context);
        if (!verifyRedirectUriForRegisteredService(context, registeredService, redirectUri)) {
            return false;
        }

        val responseType = getResponseTypeFromRequest(context);
        return verifyResponseType(context, responseType);
    }

    protected String getResponseTypeFromRequest(final WebContext context) {
        return requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE).orElse(StringUtils.EMPTY);
    }

    protected String getRedirectUriFromRequest(final WebContext context) {
        return requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI).orElse(StringUtils.EMPTY);
    }

    protected String getClientIdFromRequest(final WebContext context) {
        return requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
    }

    protected OAuthRegisteredService verifyRegisteredServiceByClientId(final WebContext context, final String clientId) throws Throwable {
        if (StringUtils.isBlank(clientId)) {
            LOGGER.warn("Missing required parameter [{}]", OAuth20Constants.CLIENT_ID);
            setErrorDetails(context, OAuth20Constants.INVALID_REQUEST, String.format("Missing required parameter: [%s]", OAuth20Constants.CLIENT_ID), false);
            return null;
        }

        LOGGER.debug("Locating registered service for client id [{}]", clientId);
        val registeredService = getRegisteredServiceByClientId(clientId);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);

        if (accessResult.isExecutionFailure()) {
            LOGGER.warn("Registered service [{}] is not found or is not authorized for access.",
                ObjectUtils.getIfNull(registeredService, clientId));
            setErrorDetails(context, OAuth20Constants.INVALID_REQUEST, StringUtils.EMPTY, false);
            return null;
        }
        return registeredService;
    }

    /**
     * Set the OAuth Error details in the context.
     *
     * @param context           the context
     * @param error             the OAuth error
     * @param errorDescription  the OAuth error description
     * @param errorWithCallBack does the error will redirect the end-user to the client
     */
    protected void setErrorDetails(final WebContext context, final String error,
                                   final String errorDescription, final boolean errorWithCallBack) {
        context.setRequestAttribute(OAuth20Constants.ERROR, error);
        context.setRequestAttribute(OAuth20Constants.ERROR_DESCRIPTION, errorDescription);
        context.setRequestAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, errorWithCallBack);
    }

    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
    }

    /**
     * Verify redirect uri for registered service boolean.
     *
     * @param context           the context
     * @param registeredService the registered service
     * @param redirectUri       the redirect uri
     * @return true/false
     */
    protected boolean verifyRedirectUriForRegisteredService(final WebContext context,
                                                            final OAuthRegisteredService registeredService,
                                                            final String redirectUri) {
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.warn("Missing required parameter [{}]", OAuth20Constants.REDIRECT_URI);
            setErrorDetails(context, OAuth20Constants.INVALID_REQUEST,
                String.format("Missing required parameter: [%s]", OAuth20Constants.REDIRECT_URI), false);
            return false;
        }

        if (!OAuth20Utils.checkCallbackValid(registeredService, redirectUri)) {
            LOGGER.warn("Redirect URI [{}] found in the request is not authorized for registered service [{}].",
                redirectUri, registeredService.getServiceId());
            setErrorDetails(context, OAuth20Constants.INVALID_REQUEST, StringUtils.EMPTY, false);
            return false;
        }
        return true;
    }

    private boolean verifyResponseType(final WebContext context, final String responseType) {
        if (StringUtils.isBlank(responseType)) {
            setErrorDetails(context, OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE,
                String.format("Missing required parameter: [%s]", OAuth20Constants.RESPONSE_TYPE), true);
            return false;
        }

        if (!OAuth20Utils.checkResponseTypes(responseType, OAuth20ResponseTypes.values())) {
            LOGGER.warn("Response type [{}] is not found in the list of supported values [{}].",
                responseType, OAuth20ResponseTypes.values());
            setErrorDetails(context, OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE,
                String.format("Unsupported response_type: [%s]", responseType), true);

            return false;
        }
        return true;
    }

}
