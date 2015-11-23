package org.jasig.cas.logout;

import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.ISOStandardDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A builder that uses the saml standard's {@code LogoutRequest} template in order
 * to build the logout request.
 * @author Misagh Moayyed
 * @see DefaultLogoutRequest
 * @since 4.0.0
 */
@Component("logoutBuilder")
public final class SamlCompliantLogoutMessageCreator implements LogoutMessageCreator {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlCompliantLogoutMessageCreator.class);
    
    /** A ticket Id generator. */
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    /** The logout request template. */
    private static final String LOGOUT_REQUEST_TEMPLATE =
            "<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"%s\" Version=\"2.0\" "
            + "IssueInstant=\"%s\"><saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">@NOT_USED@"
            + "</saml:NameID><samlp:SessionIndex>%s</samlp:SessionIndex></samlp:LogoutRequest>";

    @Override
    public String create(final LogoutRequest request) {
        final String logoutRequest = String.format(LOGOUT_REQUEST_TEMPLATE, GENERATOR.getNewTicketId("LR"),
                new ISOStandardDateFormat().getCurrentDateAndTime(), request.getTicketId());
        
        LOGGER.debug("Generated logout message: [{}]", logoutRequest);
        return logoutRequest;
    }
    
}
