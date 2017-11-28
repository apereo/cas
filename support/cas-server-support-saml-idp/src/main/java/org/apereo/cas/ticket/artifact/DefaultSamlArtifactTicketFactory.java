package org.apereo.cas.ticket.artifact;

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
    
    public DefaultSamlArtifactTicketFactory(final ExpirationPolicy expirationPolicy, final OpenSamlConfigBean configBean,
                                            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        this.expirationPolicy = expirationPolicy;
        this.configBean = configBean;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    @Override
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
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
