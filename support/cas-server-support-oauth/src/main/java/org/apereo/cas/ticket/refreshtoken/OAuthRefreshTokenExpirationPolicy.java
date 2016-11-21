package org.apereo.cas.ticket.refreshtoken;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.support.AbstractCasExpirationPolicy;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This is OAuth refresh token expiration policy (max time to live = 1 month by default).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY)
public class OAuthRefreshTokenExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -7144233906843566234L;

    /** The time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    /** No-arg constructor for serialization support. */
    public OAuthRefreshTokenExpirationPolicy() {}

    /**
     * Instantiates a new OAuth refresh token expiration policy.
     *
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    @JsonCreator
    public OAuthRefreshTokenExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return ticketState == null || ticketState.getCreationTime()
                .plus(this.timeToKillInMilliSeconds, ChronoUnit.MILLIS).isBefore(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInMilliSeconds;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        return 0L;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final OAuthRefreshTokenExpirationPolicy rhs = (OAuthRefreshTokenExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.timeToKillInMilliSeconds, rhs.timeToKillInMilliSeconds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(timeToKillInMilliSeconds)
                .toHashCode();
    }
}
