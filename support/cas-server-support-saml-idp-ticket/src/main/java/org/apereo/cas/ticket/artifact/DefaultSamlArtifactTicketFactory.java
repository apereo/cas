package org.apereo.cas.ticket.artifact;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.opensaml.saml.common.SAMLObject;

/**
 * Default OAuth access token factory.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class DefaultSamlArtifactTicketFactory implements SamlArtifactTicketFactory {

    /**
     * ExpirationPolicy for tokens.
     */
    protected final ExpirationPolicyBuilder<SamlArtifactTicket> expirationPolicy;

    /**
     * The opensaml config bean.
     */
    protected final OpenSamlConfigBean configBean;

    /**
     * The Web application service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Override
    @SneakyThrows
    public SamlArtifactTicket create(final String artifactId,
                                     final Authentication authentication,
                                     final TicketGrantingTicket ticketGrantingTicket, final String issuer,
                                     final String relyingParty, final SAMLObject samlObject) {
        try (val w = SamlUtils.transformSamlObject(this.configBean, samlObject)) {
            val codeId = createTicketIdFor(artifactId);

            val service = this.webApplicationServiceFactory.createService(relyingParty);
            val at = new SamlArtifactTicketImpl(codeId, service, authentication,
                this.expirationPolicy.buildTicketExpirationPolicy(), ticketGrantingTicket, issuer, relyingParty, w.toString());
            if (ticketGrantingTicket != null) {
                ticketGrantingTicket.getDescendantTickets().add(at.getId());
            }
            return at;
        }
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
