package org.apereo.cas.support.oauth.validator.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpRequestUtils;
import org.pac4j.core.context.J2EContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator implements OAuth20AuthorizationRequestValidator {
    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Override
    public boolean validate(final J2EContext context) {
        final HttpServletRequest request = context.getRequest();
        final boolean checkParameterExist = HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CLIENT_ID)
            && HttpRequestUtils.doesParameterExist(request, OAuth20Constants.REDIRECT_URI)
            && HttpRequestUtils.doesParameterExist(request, OAuth20Constants.RESPONSE_TYPE);

        if (!checkParameterExist) {
            LOGGER.warn("Missing required parameters (client id, redirect uri, etc) for response type [{}].", getResponseType());
            return false;
        }

        final String responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);
        if (!OAuth20Utils.checkResponseTypes(responseType, OAuth20ResponseTypes.values())) {
            LOGGER.warn("Response type [{}] is not supported.", responseType);
            return false;
        }

        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);

        final WebApplicationService service = webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
        final AuditableContext audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .build();
        final AuditableExecutionResult accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);

        if (accessResult.isExecutionFailure()) {
            LOGGER.warn("Registered service [{}] is not found or is not authorized for access.", registeredService);
            return false;
        }

        final String redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        if (!OAuth20Utils.checkCallbackValid(registeredService, redirectUri)) {
            LOGGER.warn("Callback URL [{}] is not authorized for registered service [{}].", redirectUri, registeredService);
            return false;
        }

        return OAuth20Utils.isAuthorizedResponseTypeForService(context, registeredService);
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
    public boolean supports(final J2EContext context) {
        final String grantType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return OAuth20Utils.isResponseType(grantType, getResponseType());
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
