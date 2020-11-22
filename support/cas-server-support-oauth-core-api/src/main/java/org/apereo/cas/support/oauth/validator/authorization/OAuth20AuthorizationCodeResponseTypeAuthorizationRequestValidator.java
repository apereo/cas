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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator implements OAuth20AuthorizationRequestValidator {
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

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final JEEContext context) {
        val request = context.getNativeRequest();

        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            LOGGER.warn("Missing required parameter [{}]", OAuth20Constants.CLIENT_ID);

            setErrorDetails(context,
                OAuth20Constants.INVALID_REQUEST,
                String.format("Missing required parameter: [%s]", OAuth20Constants.CLIENT_ID),
                false);

            return false;
        }

        val redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.warn("Missing required parameter [{}]", OAuth20Constants.REDIRECT_URI);

            setErrorDetails(context,
                OAuth20Constants.INVALID_REQUEST,
                String.format("Missing required parameter: [%s]", OAuth20Constants.REDIRECT_URI),
                false);

            return false;
        }

        LOGGER.debug("Locating registered service for client id [{}]", clientId);
        val registeredService = getRegisteredServiceByClientId(clientId);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);

        if (accessResult.isExecutionFailure()) {
            LOGGER.warn("Registered service [{}] is not found or is not authorized for access.", registeredService);

            setErrorDetails(context,
                OAuth20Constants.INVALID_REQUEST,
                StringUtils.EMPTY,
                false);

            return false;
        }

        if (!OAuth20Utils.checkCallbackValid(registeredService, redirectUri)) {
            LOGGER.warn("Callback URL [{}] is not authorized for registered service [{}].", redirectUri, registeredService);

            setErrorDetails(context,
                OAuth20Constants.INVALID_REQUEST,
                StringUtils.EMPTY,
                false);

            return false;
        }

        val authnRequest = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isNotBlank(authnRequest)) {
            LOGGER.warn("Self-contained authentication requests as JWTs are not accepted");

            setErrorDetails(context,
                OAuth20Constants.REQUEST_NOT_SUPPORTED,
                StringUtils.EMPTY,
                true);

            return false;
        }

        val responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);
        if (StringUtils.isBlank(responseType)) {
            setErrorDetails(context,
                OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE,
                String.format("Missing required parameter: [%s]", OAuth20Constants.RESPONSE_TYPE),
                true);

            return false;
        }

        if (!OAuth20Utils.checkResponseTypes(responseType, OAuth20ResponseTypes.values())) {
            LOGGER.warn("Response type [{}] is not found in the list of supported values [{}].",
                responseType, OAuth20ResponseTypes.values());

            setErrorDetails(context,
                OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE,
                String.format("Unsupported response_type: [%s]", responseType),
                true);

            return false;
        }

        if (!OAuth20Utils.isAuthorizedResponseTypeForService(context, registeredService)) {
            setErrorDetails(context,
                OAuth20Constants.UNAUTHORIZED_CLIENT,
                String.format("Client is not allowed to use the [%s] response_type", responseType),
                true);

            return false;
        }

        return true;
    }

    /**
     * Set the OAuth Error details in the context.
     *
     * @param context the context
     * @param error the OAuth error
     * @param errorDescription the OAuth error description
     * @param errorWithCallBack does the error will redirect the end-user to the client
     */
    public void setErrorDetails(final JEEContext context, final String error,
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


    @Override
    public boolean supports(final JEEContext context) {
        val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return OAuth20Utils.isResponseType(responseType.map(String::valueOf).orElse(StringUtils.EMPTY), getResponseType());
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.CODE;
    }
}
