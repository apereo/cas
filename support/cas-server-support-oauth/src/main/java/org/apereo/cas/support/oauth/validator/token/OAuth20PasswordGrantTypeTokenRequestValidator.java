package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20PasswordGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    public OAuth20PasswordGrantTypeTokenRequestValidator(final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                         final ServicesManager servicesManager,
                                                         final ServiceFactory webApplicationServiceServiceFactory) {
        super(registeredServiceAccessStrategyEnforcer, servicesManager, webApplicationServiceServiceFactory);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    @Override
    protected boolean validateInternal(final J2EContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {

        final HttpServletRequest request = context.getRequest();
        if (!HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CLIENT_ID)) {
            return false;
        }
        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                this.servicesManager, clientId);
        final WebApplicationService service = webApplicationServiceServiceFactory.createService(
                registeredService.getServiceId());
        final AuditableContext audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
        final AuditableExecutionResult accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        accessResult.throwExceptionIfNeeded();

        if (!isGrantTypeSupportedBy(registeredService, grantType)) {
            LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]", getGrantType(), registeredService.getServiceId());
            return false;
        }
        return true;
    }
}
