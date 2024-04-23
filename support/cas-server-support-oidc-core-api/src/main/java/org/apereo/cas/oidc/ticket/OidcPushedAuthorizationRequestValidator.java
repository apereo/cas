package org.apereo.cas.oidc.ticket;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.validator.authorization.BaseOAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link OidcPushedAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class OidcPushedAuthorizationRequestValidator extends BaseOAuth20AuthorizationRequestValidator {
    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;

    public OidcPushedAuthorizationRequestValidator(
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final TicketRegistry ticketRegistry,
        final TicketFactory ticketFactory,
        final OAuth20RequestParameterResolver requestParameterResolver) {
        super(servicesManager, webApplicationServiceServiceFactory,
            registeredServiceAccessStrategyEnforcer, requestParameterResolver);
        this.ticketRegistry = ticketRegistry;
        this.ticketFactory = ticketFactory;
    }

    @Override
    public boolean validate(final WebContext context) throws Exception {
        return FunctionUtils.doAndHandle(() -> {
            val requestUri = context.getRequestParameter(OidcConstants.REQUEST_URI).get();
            val uriToken = ticketRegistry.getTicket(requestUri, OidcPushedAuthorizationRequest.class);
            val uriFactory = (OidcPushedAuthorizationRequestFactory) ticketFactory.get(OidcPushedAuthorizationRequest.class);
            val holder = uriFactory.toAccessTokenRequest(uriToken);
            context.setRequestAttribute(OidcPushedAuthorizationRequest.class.getName(), holder);
            val givenClientId = getClientIdFromRequest(context);
            return givenClientId.equals(holder.getClientId()) && verifyRegisteredServiceByClientId(context, holder.getClientId()) != null;
        }, throwable -> false).get();
    }

    @Override
    public boolean supports(final WebContext context) {
        return context.getRequestParameter(OAuth20Constants.CLIENT_ID).isPresent()
               && context.getRequestParameter(OidcConstants.REQUEST_URI).isPresent();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
