package org.jasig.cas.support.openid.web.flow;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredential;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.jasig.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;

import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Attempts to utilize an existing single sign on session, but only if the
 * Principal of the existing session matches the new Principal. Note that care
 * should be taken when using credentials that are automatically provided and
 * not entered by the user.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("openIdSingleSignOnAction")
public final class OpenIdSingleSignOnAction extends AbstractNonInteractiveCredentialsAction {

    @NotNull
    private OpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();

    public void setExtractor(final OpenIdUserNameExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final String openidIdentityParameter = context.getRequestParameters().get(OpenIdProtocolConstants.OPENID_IDENTITY);
        String userName = null;
        if (OpenIdProtocolConstants.OPENID_IDENTIFIERSELECT.equals(openidIdentityParameter)) {
            userName = OpenIdProtocolConstants.OPENID_IDENTIFIERSELECT;
            context.getExternalContext().getSessionMap().remove(OpenIdProtocolConstants.OPENID_LOCALID);
            // already authenticated: retrieve the username from the authentication
            if (ticketGrantingTicketId != null) {
                try {
                    final TicketGrantingTicket tgt = getCentralAuthenticationService()
                            .getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
                    userName = tgt.getAuthentication().getPrincipal().getId();
                } catch (final InvalidTicketException e) {
                    logger.error("Cannot get TGT", e);
                }
            }
        } else {
            userName = this.extractor.extractLocalUsernameFromUri(openidIdentityParameter);
            context.getExternalContext().getSessionMap().put(OpenIdProtocolConstants.OPENID_LOCALID, userName);
        }
        final Service service = WebUtils.getService(context);

        // clear the service because otherwise we can fake the username
        if (service instanceof OpenIdService && userName == null) {
            context.getFlowScope().remove("service");
        }

        if (ticketGrantingTicketId == null || userName == null) {
            return null;
        }

        return new OpenIdCredential(
                ticketGrantingTicketId, userName);
    }
}
