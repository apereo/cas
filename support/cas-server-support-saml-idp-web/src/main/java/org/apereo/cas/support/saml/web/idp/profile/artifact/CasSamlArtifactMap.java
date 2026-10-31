package org.apereo.cas.support.saml.web.idp.profile.artifact;

import module java.base;
import org.apereo.cas.ticket.TicketFactory;
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

    @Override
    public void put(
        @NonNull final String artifact,
        @NonNull final String relyingPartyId,
        @NonNull final String issuerId,
        @NonNull final SAMLObject samlMessage)
            throws IOException {
        super.put(artifact, relyingPartyId, issuerId, samlMessage);

        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            ticketGrantingTicketCookieGenerator, ticketRegistry, request);

        val samlArtifactTicketFactory = (SamlArtifactTicketFactory) ticketFactory.get(SamlArtifactTicket.class);
        val ticket = samlArtifactTicketFactory.create(artifact,
            Objects.requireNonNull(ticketGrantingTicket).getAuthentication(),
            ticketGrantingTicket,
            issuerId,
            relyingPartyId, samlMessage);
        FunctionUtils.doUnchecked(_ -> ticketRegistry.addTicket(ticket));
    }
}
