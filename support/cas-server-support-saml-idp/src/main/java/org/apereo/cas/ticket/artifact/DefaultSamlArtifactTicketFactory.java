package org.apereo.cas.ticket.artifact;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.opensaml.saml.common.SAMLObject;

import java.io.StringWriter;

/**
 * Default OAuth access token factory.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultSamlArtifactTicketFactory implements SamlArtifactTicketFactory {
    
    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicy expirationPolicy;

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
        try (StringWriter w = SamlUtils.transformSamlObject(this.configBean, samlObject)) {
            final String codeId = createTicketIdFor(artifactId);
            
            final Service service = this.webApplicationServiceFactory.createService(relyingParty);
            final SamlArtifactTicket at = new SamlArtifactTicketImpl(codeId, service, authentication,
                    this.expirationPolicy, ticketGrantingTicket, issuer, relyingParty, w.toString());
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
