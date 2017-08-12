package org.apereo.cas.support.openid.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.support.openid.authentication.principal.OpenIdCredential;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.apereo.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * Attempts to utilize an existing single sign on session, but only if the
 * Principal of the existing session matches the new Principal. Note that care
 * should be taken when using credentials that are automatically provided and
 * not entered by the user.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdSingleSignOnAction extends AbstractNonInteractiveCredentialsAction {

    private OpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();
    private final TicketRegistrySupport ticketRegistrySupport;

    public OpenIdSingleSignOnAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                    final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                    final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy, final OpenIdUserNameExtractor extractor,
                                    final TicketRegistrySupport ticketRegistrySupport) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.extractor = extractor;
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final String openidIdentityParameter = context.getRequestParameters().get(OpenIdProtocolConstants.OPENID_IDENTITY);

        final String userName = getOpenIdSelectedIdentifier(context, ticketGrantingTicketId, openidIdentityParameter);
        final Service service = WebUtils.getService(context);

        // clear the service because otherwise we can fake the username
        if (service instanceof OpenIdService && StringUtils.isBlank(userName)) {
            context.getFlowScope().remove(CasProtocolConstants.PARAMETER_SERVICE);
        }

        if (StringUtils.isBlank(ticketGrantingTicketId) || StringUtils.isBlank(userName)) {
            return null;
        }

        return new OpenIdCredential(ticketGrantingTicketId, userName);
    }

    private String getOpenIdSelectedIdentifier(final RequestContext context, final String ticketGrantingTicketId,
                                               final String openidIdentityParameter) {
        if (OpenIdProtocolConstants.OPENID_IDENTIFIERSELECT.equals(openidIdentityParameter)) {
            context.getFlowScope().remove(OpenIdProtocolConstants.OPENID_LOCALID);
            final Principal p = ticketRegistrySupport.getAuthenticatedPrincipalFrom(ticketGrantingTicketId);
            if (p != null) {
                return p.getId();
            }
            return OpenIdProtocolConstants.OPENID_IDENTIFIERSELECT;
        }
        
        final String userName = this.extractor.extractLocalUsernameFromUri(openidIdentityParameter);
        context.getFlowScope().put(OpenIdProtocolConstants.OPENID_LOCALID, userName);
        return userName;
    }
}
