package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20PasswordGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    public OAuth20PasswordGrantTypeTokenRequestValidator(final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                         final ServicesManager servicesManager,
                                                         final ServiceFactory webApplicationServiceServiceFactory) {
        super(registeredServiceAccessStrategyEnforcer);
        this.servicesManager = servicesManager;
        this.webApplicationServiceServiceFactory = webApplicationServiceServiceFactory;
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    @Override
    protected boolean validateInternal(final J2EContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        val request = context.getRequest();
        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);

        if (HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CLIENT_ID)) {
            val service = webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
            return !accessResult.isExecutionFailure();
        }
        return false;
    }
}
