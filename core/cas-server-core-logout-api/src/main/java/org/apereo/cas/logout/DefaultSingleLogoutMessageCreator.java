package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
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
 * @see DefaultSingleLogoutRequest
 * @since 4.0.0
 */
@Slf4j
public class DefaultSingleLogoutMessageCreator implements SingleLogoutMessageCreator {

    /**
     * A ticket Id generator.
     */
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator(18);

    /**
     * The logout request template.
     */
    private static final String LOGOUT_REQUEST_TEMPLATE =
        "<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"%s\" Version=\"2.0\" "
            + "IssueInstant=\"%s\"><saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">@NOT_USED@"
            + "</saml:NameID><samlp:SessionIndex>%s</samlp:SessionIndex></samlp:LogoutRequest>";

    @Override
    public String create(final SingleLogoutRequest request) {
        val logoutRequest = String.format(LOGOUT_REQUEST_TEMPLATE, GENERATOR.getNewTicketId("LR"),
            new ISOStandardDateFormat().getCurrentDateAndTime(), request.getTicketId());
        LOGGER.trace("Attempting to deflate the logout message [{}]", logoutRequest);
        return CompressionUtils.deflate(logoutRequest);
    }

}
