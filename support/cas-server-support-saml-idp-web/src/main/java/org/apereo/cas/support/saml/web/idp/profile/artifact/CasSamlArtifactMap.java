package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.impl.BasicSAMLArtifactMap;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;

import java.io.IOException;
import java.util.Objects;

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

    private final SessionStore<JEEContext> samlIdPDistributedSessionStore;

    private final CentralAuthenticationService centralAuthenticationService;

    @Override
    public void put(final String artifact, final String relyingPartyId,
                    final String issuerId, final SAMLObject samlMessage) throws IOException {
        super.put(artifact, relyingPartyId, issuerId, samlMessage);

        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        var ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            ticketGrantingTicketCookieGenerator, ticketRegistry, request);
        if (ticketGrantingTicket == null) {
            ticketGrantingTicket = samlIdPDistributedSessionStore
                .get(new JEEContext(request, response, samlIdPDistributedSessionStore), WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID)
                .map(ticketId -> centralAuthenticationService.getTicket(ticketId.toString(), TicketGrantingTicket.class))
                .orElse(null);
        }

        val ticket = samlArtifactTicketFactory.create(artifact,
            Objects.requireNonNull(ticketGrantingTicket).getAuthentication(),
            ticketGrantingTicket,
            issuerId,
            relyingPartyId, samlMessage);
        ticketRegistry.addTicket(ticket);
    }
}
