package org.apereo.cas.services;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.util.model.TriStateBoolean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.core.Ordered;

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
@FunctionalInterface
public interface RegisteredServiceSingleSignOnParticipationPolicy extends Serializable, Ordered {

    /**
     * Should registered service participate in sso?
     *
     * @param ticketState the ticket state
     * @return true/false
     */
    @JsonIgnore
    boolean shouldParticipateInSso(TicketState ticketState);

    @Override
    default int getOrder() {
        return 0;
    }


    /**
     * Flag that indicates whether to create SSO session on re-newed authentication event
     * when dealing with this service.
     *
     * @return true/false
     */
    default TriStateBoolean isCreateCookieOnRenewedAuthentication() {
        return TriStateBoolean.UNDEFINED;
    }
}
