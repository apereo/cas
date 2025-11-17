package org.apereo.cas.support.saml.web.idp.profile.artifact;

import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.impl.BasicSAMLArtifactMap;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
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

    private final TicketFactory ticketFactory;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final SessionStore samlIdPDistributedSessionStore;

    @Override
    public void put(
        @NonNull final String artifact,
        @NonNull final String relyingPartyId,
        @NonNull final String issuerId,
        @NonNull final SAMLObject samlMessage) throws IOException {
        super.put(artifact, relyingPartyId, issuerId, samlMessage);

        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        var ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            ticketGrantingTicketCookieGenerator, ticketRegistry, request);
        if (ticketGrantingTicket == null) {
            val ctx = new JEEContext(request, response);
            val manager = new ProfileManager(ctx, samlIdPDistributedSessionStore);
            ticketGrantingTicket = manager.getProfile()
                .map(profile -> profile.getAttribute(TicketGrantingTicket.class.getName()))
                .map(ticketId -> ticketRegistry.getTicket(ticketId.toString(), TicketGrantingTicket.class))
                .orElse(null);
        }

        val samlArtifactTicketFactory = (SamlArtifactTicketFactory) ticketFactory.get(SamlArtifactTicket.class);
        val ticket = samlArtifactTicketFactory.create(artifact,
            Objects.requireNonNull(ticketGrantingTicket).getAuthentication(),
            ticketGrantingTicket,
            issuerId,
            relyingPartyId, samlMessage);
        FunctionUtils.doUnchecked(_ -> ticketRegistry.addTicket(ticket));
    }
}
