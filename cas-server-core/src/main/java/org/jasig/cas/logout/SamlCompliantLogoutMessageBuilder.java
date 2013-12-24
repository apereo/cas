package org.jasig.cas.logout;

import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.SamlDateUtils;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder that uses the saml standard's <code>LogoutRequest</code> template in order
 * to build the logout request.
 * @author Misagh Moayyed
 * @since 4.0
 * @see LogoutRequest
 */
public final class SamlCompliantLogoutMessageBuilder implements LogoutMessageBuilder {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlCompliantLogoutMessageBuilder.class);
    
    /** A ticket Id generator. */
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    /** The logout request template. */
    private static final String LOGOUT_REQUEST_TEMPLATE =
            "<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"%s\" Version=\"2.0\" "
            + "IssueInstant=\"%s\"><saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">@NOT_USED@"
            + "</saml:NameID><samlp:SessionIndex>%s</samlp:SessionIndex></samlp:LogoutRequest>";

    @Override
    public String build(final LogoutRequest request) {
        final String logoutRequest = String.format(LOGOUT_REQUEST_TEMPLATE, GENERATOR.getNewTicketId("LR"),
                SamlDateUtils.getCurrentDateAndTime(), request.getTicketId());
        
        LOGGER.debug("Generated back-channel logout message: [{}]", logoutRequest);
        return logoutRequest;
    }
    
}
