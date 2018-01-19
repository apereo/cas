package org.apereo.cas.ticket.query;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
 * Factory to create OAuth access tokens.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultSamlAttributeQueryTicketFactory implements SamlAttributeQueryTicketFactory {

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicy expirationPolicy;

    /**
     * The Web application service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * The opensaml config bean.
     */
    protected final OpenSamlConfigBean configBean;
    
    @Override
    @SneakyThrows
    public SamlAttributeQueryTicket create(final String id, final SAMLObject samlObject,
                                           final String relyingParty, final TicketGrantingTicket ticketGrantingTicket) {
        try (StringWriter w = SamlUtils.transformSamlObject(this.configBean, samlObject)) {
            final String codeId = createTicketIdFor(id);
            final Service service = this.webApplicationServiceFactory.createService(relyingParty);
            final SamlAttributeQueryTicket at = new SamlAttributeQueryTicketImpl(codeId, service, this.expirationPolicy, 
                    relyingParty, w.toString(), ticketGrantingTicket);
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
