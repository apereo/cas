package org.apereo.cas.oidc.validator.authorization;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.util.HttpRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

/**
 * This is {@link OidcAuthorizationCodeResponseTypeAuthorizationRequestValidator}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
public class OidcAuthorizationCodeResponseTypeAuthorizationRequestValidator
    extends OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator
    implements OAuth20AuthorizationRequestValidator {
    public OidcAuthorizationCodeResponseTypeAuthorizationRequestValidator(final ServicesManager servicesManager,
                                                                          final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                                          final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @Override
    protected boolean doRequiredParametersExist(final HttpServletRequest request) {
        return Stream.of(OAuth20Constants.SCOPE, OAuth20Constants.CLIENT_ID, OAuth20Constants.REDIRECT_URI, OAuth20Constants.RESPONSE_TYPE)
            .allMatch(s -> HttpRequestUtils.doesParameterExist(request, s));
    }
}
