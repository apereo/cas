package org.apereo.cas.support.openid.authentication.handler.support;

import java.security.GeneralSecurityException;

import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.openid.authentication.principal.OpenIdCredential;

import javax.security.auth.login.FailedLoginException;

/**
 * Ensures that the OpenId provided matches with the existing
 * TicketGrantingTicket. Otherwise, fail authentication.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {
    
    private TicketRegistry ticketRegistry;

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final OpenIdCredential c = (OpenIdCredential) credential;

        final TicketGrantingTicket t = this.ticketRegistry.getTicket(c.getTicketGrantingTicketId(),
                        TicketGrantingTicket.class);

        if (t == null || t.isExpired()) {
            throw new FailedLoginException("TGT is null or expired.");
        }
        final Principal principal = t.getAuthentication().getPrincipal();
        if (!principal.getId().equals(c.getUsername())) {
            throw new FailedLoginException("Principal ID mismatch");
        }
        return new DefaultHandlerResult(this, new BasicCredentialMetaData(c), principal);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenIdCredential;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
