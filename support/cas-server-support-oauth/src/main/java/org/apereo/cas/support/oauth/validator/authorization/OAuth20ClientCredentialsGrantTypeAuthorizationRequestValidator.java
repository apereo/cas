package org.apereo.cas.support.oauth.validator.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpRequestUtils;
import org.pac4j.core.context.J2EContext;

/**
 * This is {@link OAuth20ClientCredentialsGrantTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20ClientCredentialsGrantTypeAuthorizationRequestValidator implements OAuth20AuthorizationRequestValidator {
    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Override
    public boolean validate(final J2EContext context) {
        final var request = context.getRequest();

        if (!HttpRequestUtils.doesParameterExist(request, OAuth20Constants.GRANT_TYPE)) {
            LOGGER.warn("Grant type must be specified");
            return false;
        }

        final var grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);

        if (!HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CLIENT_ID)) {
            LOGGER.warn("Client id not specified for grant type [{}]", grantType);
            return false;
        }

        if (!HttpRequestUtils.doesParameterExist(request, OAuth20Constants.SECRET)) {
            LOGGER.warn("Client secret is not specified for grant type [{}]", grantType);
            return false;
        }

        final var clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
        final var registeredService = getRegisteredServiceByClientId(clientId);
        final var service = webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
        final var audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .build();
        final var accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);

        if (accessResult.isExecutionFailure()) {
            LOGGER.warn("Registered service [{}] is not found or is not authorized for access.", registeredService);
            return false;
        }

        return OAuth20Utils.isAuthorizedGrantTypeForService(context, registeredService);

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
        final var grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }
}
