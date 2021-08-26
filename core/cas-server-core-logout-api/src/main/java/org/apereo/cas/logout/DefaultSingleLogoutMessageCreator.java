package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.ISOStandardDateFormat;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * A builder that uses the saml standard's {@code LogoutRequest} template in order
 * to build the logout request.
 *
 * @author Misagh Moayyed
 * @see DefaultSingleLogoutRequestContext
 * @since 4.0.0
 */
@Slf4j
@SuppressWarnings("InlineFormatString")
public class DefaultSingleLogoutMessageCreator implements SingleLogoutMessageCreator {

    /**
     * A ticket Id generator.
     */
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator(18);
    
    @Override
    public SingleLogoutMessage create(final SingleLogoutRequestContext request) {
        val logoutRequest = String.format("<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"%s\" Version=\"2.0\" "
                + "IssueInstant=\"%s\"><saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">%s"
                + "</saml:NameID><samlp:SessionIndex>%s</samlp:SessionIndex></samlp:LogoutRequest>",
            GENERATOR.getNewTicketId("LR"),
            new ISOStandardDateFormat().getCurrentDateAndTime(),
            request.getExecutionRequest().getTicketGrantingTicket().getAuthentication().getPrincipal().getId(),
            request.getTicketId());

        val builder = SingleLogoutMessage.builder();
        if (request.getLogoutType() == RegisteredServiceLogoutType.FRONT_CHANNEL) {
            LOGGER.trace("Attempting to deflate the logout message [{}]", logoutRequest);
            return builder.payload(CompressionUtils.deflate(logoutRequest)).build();
        }
        return builder.payload(logoutRequest).build();
    }
}
