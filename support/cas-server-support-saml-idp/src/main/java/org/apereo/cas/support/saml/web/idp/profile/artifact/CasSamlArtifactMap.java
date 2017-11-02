package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieUtils;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.impl.BasicSAMLArtifactMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This is {@link CasSamlArtifactMap}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasSamlArtifactMap extends BasicSAMLArtifactMap {

    private final TicketRegistry ticketRegistry;
    private final SamlArtifactTicketFactory samlArtifactTicketFactory;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    public CasSamlArtifactMap(final TicketRegistry ticketRegistry, final SamlArtifactTicketFactory samlArtifactTicketFactory,
                              final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketRegistry = ticketRegistry;
        this.samlArtifactTicketFactory = samlArtifactTicketFactory;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    @Override
    public void put(final String artifact, final String relyingPartyId,
                    final String issuerId, final SAMLObject samlMessage) throws IOException {
        super.put(artifact, relyingPartyId, issuerId, samlMessage);

        final HttpServletRequest request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        final TicketGrantingTicket ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
                ticketGrantingTicketCookieGenerator, this.ticketRegistry, request);

        final SamlArtifactTicket ticket = samlArtifactTicketFactory.create(artifact,
                ticketGrantingTicket.getAuthentication(),
                ticketGrantingTicket,
                issuerId,
                relyingPartyId, samlMessage);
        this.ticketRegistry.addTicket(ticket);
    }
}
