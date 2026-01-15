package org.apereo.cas.support.oauth.validator.authorization;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator extends
    OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator {

    public OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator(
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final OAuth20RequestParameterResolver requestParameterResolver) {
        super(servicesManager, webApplicationServiceServiceFactory,
            registeredServiceAccessStrategyEnforcer, requestParameterResolver);
    }

    @Override
    public boolean supports(final WebContext context) throws Throwable {
        val challenge = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CODE_VERIFIER)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return StringUtils.isNotBlank(challenge) && super.supports(context);
    }
}
