package org.apereo.cas.services;


import org.apereo.cas.ticket.AuthenticationAwareTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy extends BaseDateTimeRegisteredServiceSingleSignOnParticipationPolicy {
    @Serial
    private static final long serialVersionUID = -5923946898337761319L;

    public LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(final TimeUnit timeUnit, final long timeValue, final int order) {
        super(timeUnit, timeValue, order);
    }

    @Override
    protected ZonedDateTime determineInitialDateTime(final RegisteredService registeredService, final AuthenticationAwareTicket ticketState) {
        return ticketState.getLastTimeUsed();
    }
}
