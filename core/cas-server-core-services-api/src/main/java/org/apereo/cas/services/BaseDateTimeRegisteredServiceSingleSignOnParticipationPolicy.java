package org.apereo.cas.services;

import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link BaseDateTimeRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public abstract class BaseDateTimeRegisteredServiceSingleSignOnParticipationPolicy extends DefaultRegisteredServiceSingleSignOnParticipationPolicy {
    private static final long serialVersionUID = -5923946898337761319L;

    private TimeUnit timeUnit = TimeUnit.SECONDS;

    private long timeValue;

    private int order;

    @Override
    public boolean shouldParticipateInSso(final TicketState ticketState) {
        LOGGER.trace("Calculating SSO participation criteria for [{}]", ticketState);
        if (timeValue <= 0) {
            return true;
        }

        val convertedNano = timeUnit.toNanos(timeValue);
        val startingDate = determineInitialDateTime(ticketState);
        val endingDate = startingDate.plusNanos(convertedNano);
        val currentTime = ZonedDateTime.now(ZoneOffset.UTC);

        LOGGER.trace("Starting date/time [{}]. Ending date/time constraint [{}]. Current date/time [{}]",
            startingDate, endingDate, currentTime);

        if (currentTime.isBefore(endingDate)) {
            LOGGER.debug("Current time [{}] is before [{}] where SSO participation is granted", currentTime, endingDate);
            return true;
        }

        LOGGER.debug("Current time [{}] is after [{}] where SSO participation is rejected", currentTime, endingDate);
        return false;
    }

    /**
     * Determine initial date time zoned date time.
     *
     * @param ticketState the ticket state
     * @return the zoned date time
     */
    @JsonIgnore
    protected abstract ZonedDateTime determineInitialDateTime(TicketState ticketState);
}
