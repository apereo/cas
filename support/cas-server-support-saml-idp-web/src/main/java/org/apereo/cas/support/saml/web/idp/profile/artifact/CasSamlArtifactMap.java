package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.impl.BasicSAMLArtifactMap;

import java.io.IOException;

/**
 * This is {@link CasSamlArtifactMap}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class CasSamlArtifactMap extends BasicSAMLArtifactMap {

    private final TicketRegistry ticketRegistry;

    private final SamlArtifactTicketFactory samlArtifactTicketFactory;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Override
    public void put(final String artifact, final String relyingPartyId,
                    final String issuerId, final SAMLObject samlMessage) throws IOException {
        super.put(artifact, relyingPartyId, issuerId, samlMessage);

        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            ticketGrantingTicketCookieGenerator, this.ticketRegistry, request);

        val ticket = samlArtifactTicketFactory.create(artifact,
            ticketGrantingTicket.getAuthentication(),
            ticketGrantingTicket,
            issuerId,
            relyingPartyId, samlMessage);
        this.ticketRegistry.addTicket(ticket);
    }
}
