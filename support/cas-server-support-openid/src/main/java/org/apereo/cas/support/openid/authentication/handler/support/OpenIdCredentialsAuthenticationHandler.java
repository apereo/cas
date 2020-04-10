package org.apereo.cas.support.openid.authentication.handler.support;

import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.principal.OpenIdCredential;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Ensures that the OpenId provided matches with the existing
 * TicketGrantingTicket. Otherwise, fail authentication.
 *
 * @author Scott Battaglia
 * @deprecated 6.2
 * @since 3.1
 */
@Deprecated(since = "6.2.0")
public class OpenIdCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    private final TicketRegistry ticketRegistry;

    public OpenIdCredentialsAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                  final PrincipalFactory principalFactory,
                                                  final TicketRegistry ticketRegistry,
                                                  final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential) throws GeneralSecurityException {
        val c = (OpenIdCredential) credential;

        val t = this.ticketRegistry.getTicket(c.getTicketGrantingTicketId(), TicketGrantingTicket.class);

        if (t == null || t.isExpired()) {
            throw new FailedLoginException("Ticket-granting ticket is null or expired.");
        }
        val principal = t.getAuthentication().getPrincipal();
        if (!principal.getId().equals(c.getUsername())) {
            throw new FailedLoginException("Principal ID mismatch");
        }
        return new DefaultAuthenticationHandlerExecutionResult(this, new BasicCredentialMetaData(c), principal);
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return OpenIdCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenIdCredential;
    }

}
