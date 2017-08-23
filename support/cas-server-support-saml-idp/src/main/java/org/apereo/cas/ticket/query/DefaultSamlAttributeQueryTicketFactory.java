package org.apereo.cas.ticket.query;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketImpl;

import java.io.StringWriter;
import java.util.Map;

/**
 * Factory to create OAuth access tokens.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultSamlAttributeQueryTicketFactory implements SamlAttributeQueryTicketFactory {

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicy expirationPolicy;

    /**
     * The Web application service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    public DefaultSamlAttributeQueryTicketFactory(final ExpirationPolicy expirationPolicy, final OpenSamlConfigBean configBean,
                                                  final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        this.expirationPolicy = expirationPolicy;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    @Override
    public SamlAttributeQueryTicket create(final String id, final Map<String, Object> attributes,
                                           final String issuer, final TicketGrantingTicket ticketGrantingTicket) {
        try {
            final String codeId = createTicketIdFor(id);
            final Service service = this.webApplicationServiceFactory.createService(issuer);
            final SamlAttributeQueryTicket at = new SamlAttributeQueryTicketImpl(codeId, service, this.expirationPolicy, issuer, attributes);
            if (ticketGrantingTicket != null) {
                ticketGrantingTicket.getDescendantTickets().add(at.getId());
            }
            return at;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
