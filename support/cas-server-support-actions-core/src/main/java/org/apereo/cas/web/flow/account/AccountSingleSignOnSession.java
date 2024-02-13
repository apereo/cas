package org.apereo.cas.web.flow.account;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.metadata.ClientInfoAuthenticationMetaDataPopulator;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ISOStandardDateFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link AccountSingleSignOnSession}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
class AccountSingleSignOnSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 8935451143814878214L;

    private String payload;

    private final String principal;

    private final String authenticationDate;

    private final String userAgent;

    private final String clientIpAddress;

    private String geoLocation;

    private final String id;

    AccountSingleSignOnSession(final TicketGrantingTicket ticket) {
        this.id = ticket.getId();
        this.principal = ticket.getAuthentication().getPrincipal().getId();
        this.userAgent = ticket.getAuthentication().getCredentials()
            .stream()
            .map(Credential::getCredentialMetadata)
            .filter(cred -> cred.getProperties().containsKey(CredentialMetadata.PROPERTY_USER_AGENT))
            .map(cred -> cred.getProperties().get(CredentialMetadata.PROPERTY_USER_AGENT).toString())
            .findFirst()
            .orElse(StringUtils.EMPTY);
        this.clientIpAddress = CollectionUtils.firstElement(ticket.getAuthentication()
                .getAttributes()
                .get(ClientInfoAuthenticationMetaDataPopulator.ATTRIBUTE_CLIENT_IP_ADDRESS))
            .map(Object::toString)
            .orElse(StringUtils.EMPTY);
        val dateFormat = new ISOStandardDateFormat();
        this.authenticationDate = dateFormat.format(DateTimeUtils.dateOf(ticket.getAuthentication().getAuthenticationDate()));
    }
}
