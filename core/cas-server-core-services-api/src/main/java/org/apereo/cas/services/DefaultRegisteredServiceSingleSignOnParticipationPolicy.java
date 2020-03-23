package org.apereo.cas.services;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.util.model.TriStateBoolean;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DefaultRegisteredServiceSingleSignOnParticipationPolicy}.
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
public class DefaultRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    private static final long serialVersionUID = -1223944598337761319L;

    private TriStateBoolean createCookieOnRenewedAuthentication;

    @Override
    public boolean shouldParticipateInSso(final TicketState ticketState) {
        return true;
    }
}
