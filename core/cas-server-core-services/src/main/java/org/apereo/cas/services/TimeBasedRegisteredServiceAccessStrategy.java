package org.apereo.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * The {@link TimeBasedRegisteredServiceAccessStrategy} is responsible for
 * enforcing CAS authorization strategy based on a configured start/end time.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TimeBasedRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -6180748828025837047L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeBasedRegisteredServiceAccessStrategy.class);

    private String startingDateTime;

    private String endingDateTime;

    /**
     * Initiates the time-based access strategy.
     */
    public TimeBasedRegisteredServiceAccessStrategy() {
    }

    /**
     * Initiates the time-based access strategy.
     *
     * @param enabled    is service access allowed?
     * @param ssoEnabled is service allowed to take part in SSO?
     */
    public TimeBasedRegisteredServiceAccessStrategy(final boolean enabled, final boolean ssoEnabled) {
        super(enabled, ssoEnabled);
    }

    public String getStartingDateTime() {
        return this.startingDateTime;
    }

    public String getEndingDateTime() {
        return this.endingDateTime;
    }

    public void setStartingDateTime(final String startingDateTime) {
        this.startingDateTime = startingDateTime;
    }

    public void setEndingDateTime(final String endingDateTime) {
        this.endingDateTime = endingDateTime;
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
        final TimeBasedRegisteredServiceAccessStrategy rhs = (TimeBasedRegisteredServiceAccessStrategy) obj;

        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.startingDateTime, rhs.startingDateTime)
                .append(this.endingDateTime, rhs.endingDateTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.startingDateTime)
                .append(this.endingDateTime)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("startingDateTime", this.startingDateTime)
                .append("endingDateTime", this.endingDateTime)
                .toString();
    }

    @Override
    public boolean isServiceAccessAllowed() {

        if (!doesStartingTimeAllowServiceAccess()) {
            return false;
        }

        if (!doesEndingTimeAllowServiceAccess()) {
            return false;
        }

        return super.isServiceAccessAllowed();
    }

    /**
     * Does ending time allow service access boolean.
     *
     * @return true/false
     */
    protected boolean doesEndingTimeAllowServiceAccess() {
        if (this.endingDateTime != null) {
            final ZonedDateTime et = DateTimeUtils.zonedDateTimeOf(this.endingDateTime);

            if (et != null) {
                if (ZonedDateTime.now().isAfter(et)) {
                    LOGGER.warn("Service access not allowed because it ended at [{}]. Now is [{}]", this.endingDateTime, ZonedDateTime.now());
                    return false;
                }
            } else {
                final LocalDateTime etLocal = DateTimeUtils.localDateTimeOf(this.endingDateTime);
                if (etLocal != null) {
                    if (LocalDateTime.now().isAfter(etLocal)) {
                        LOGGER.warn("Service access not allowed because it ended at [{}]. Now is [{}]", this.endingDateTime, LocalDateTime.now());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Does starting time allow service access boolean.
     *
     * @return true/false
     */
    protected boolean doesStartingTimeAllowServiceAccess() {
        if (this.startingDateTime != null) {
            final ZonedDateTime st = DateTimeUtils.zonedDateTimeOf(this.startingDateTime);
            if (st != null) {
                if (ZonedDateTime.now().isBefore(st)) {
                    LOGGER.warn("Service access not allowed because it starts at [{}]. Zoned now is [{}]", this.startingDateTime, ZonedDateTime.now());
                    return false;
                }
            } else {
                final LocalDateTime stLocal = DateTimeUtils.localDateTimeOf(this.startingDateTime);
                if (stLocal != null) {
                    if (LocalDateTime.now().isBefore(stLocal)) {
                        LOGGER.warn("Service access not allowed because it starts at [{}]. Local now is [{}]", this.startingDateTime, ZonedDateTime.now());
                        return false;
                    }
                }
            }

        }
        return true;
    }
}
