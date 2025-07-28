package org.apereo.cas.ticket.artifact;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    @Getter
    protected final ExpirationPolicyBuilder<SamlArtifactTicket> expirationPolicyBuilder;

    /**
     * The opensaml config bean.
     */
    protected final OpenSamlConfigBean configBean;

    /**
     * The Web application service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    protected final TicketTrackingPolicy descendantTicketsTrackingPolicy;

    @Getter
    protected final UniqueTicketIdGenerator ticketIdGenerator = UniqueTicketIdGenerator.prefixedTicketIdGenerator();
    
    @Override
    public SamlArtifactTicket create(final String artifactId,
                                     final Authentication authentication,
                                     final TicketGrantingTicket ticketGrantingTicket, final String issuer,
                                     final String relyingParty, final SAMLObject samlObject) {
        return FunctionUtils.doUnchecked(() -> {
            try (val w = SamlUtils.transformSamlObject(this.configBean, samlObject)) {
                val codeId = createTicketIdFor(artifactId);

                val service = webApplicationServiceFactory.createService(relyingParty);
                val at = new SamlArtifactTicketImpl(codeId, service, authentication,
                    this.expirationPolicyBuilder.buildTicketExpirationPolicy(), ticketGrantingTicket, issuer, relyingParty, w.toString());

                descendantTicketsTrackingPolicy.trackTicket(ticketGrantingTicket, at);

                return at;
            }
        });
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return SamlArtifactTicket.class;
    }
}
