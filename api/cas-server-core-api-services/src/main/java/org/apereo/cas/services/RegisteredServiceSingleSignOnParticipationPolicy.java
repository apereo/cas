package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.validation.Assertion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceSingleSignOnParticipationPolicy}.
 * This contract allows applications registered with CAS to define
 * an expiration policy for SSO sessions. For example, an application
 * may be decide to opt out of participating in SSO, if one exists,
 * if the existing SSO session is somewhat stale or idle given
 * certain timestamps. Likewise, validation events may start to issue
 * failures if the validated assertion is before/after a certain timestamp
 * or fails to pass other requirements for the specific registered service.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceSingleSignOnParticipationPolicy extends Serializable {
    
    @JsonIgnore
    boolean shouldParticipateInSso(TicketGrantingTicket ticketGrantingTicket);

    @JsonIgnore
    boolean isAcceptableForSso(Assertion authentication);
}
