package org.apereo.cas.ticket.proxy.support;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.util.http.HttpClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Proxy Handler to handle the default callback functionality of CAS 2.0.
 * <p>
 * The default behavior as defined in the CAS 2 Specification is to callback the
 * URL provided and give it a pgtIou and a pgtId.
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class Cas20ProxyHandler implements ProxyHandler {
    private static final int BUFFER_LENGTH_ADDITIONAL_CHARGE = 15;

    private final HttpClient httpClient;
    private final UniqueTicketIdGenerator uniqueTicketIdGenerator;

    @Override
    public String handle(final Credential credential, final TicketGrantingTicket proxyGrantingTicketId) {
        val serviceCredentials = (HttpBasedServiceCredential) credential;
        val proxyIou = this.uniqueTicketIdGenerator.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX);

        val callbackUrl = serviceCredentials.getCallbackUrl();
        val serviceCredentialsAsString = callbackUrl.toExternalForm();
        val bufferLength = serviceCredentialsAsString.length() + proxyIou.length()
            + proxyGrantingTicketId.getId().length() + BUFFER_LENGTH_ADDITIONAL_CHARGE;

        val stringBuffer = new StringBuilder(bufferLength)
            .append(serviceCredentialsAsString);

        if (callbackUrl.getQuery() != null) {
            stringBuffer.append('&');
        } else {
            stringBuffer.append('?');
        }

        stringBuffer.append(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU)
            .append('=')
            .append(proxyIou)
            .append('&')
            .append(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_ID)
            .append('=')
            .append(proxyGrantingTicketId);

        if (this.httpClient.isValidEndPoint(stringBuffer.toString())) {
            LOGGER.debug("Sent ProxyIou of [{}] for service: [{}]", proxyIou, serviceCredentials);
            return proxyIou;
        }

        LOGGER.debug("Failed to send ProxyIou of [{}] for service: [{}]", proxyIou, serviceCredentials);
        return null;
    }

    @Override
    public boolean canHandle(final Credential credential) {
        return true;
    }
}
