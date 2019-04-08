package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.validation.Assertion;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CreationTimeConstraintRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreationTimeConstraintRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    private static final long serialVersionUID = -5923946898337761319L;

    private TimeUnit timeUnit = TimeUnit.SECONDS;

    private long timeValue;

    @Override
    public boolean shouldParticipateInSso(final TicketGrantingTicket ticketGrantingTicket) {
        return false;
    }

    @Override
    public boolean isAcceptableForSso(final Assertion authentication) {
        return false;
    }
}
