package org.jasig.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * The {@link TimeBasedRegisteredServiceAccessStrategy} is responsible for
 * enforcing CAS authorization strategy based on a configured start/end time.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TimeBasedRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -6180748828025837047L;

    private String startingDateTime;

    private String endingDateTime;

    /**
     * Initiates the time-based access strategy.
     */
    public TimeBasedRegisteredServiceAccessStrategy() {
    }

    /**
     * Initiates the time-based access strategy.
     * @param enabled is service access allowed?
     * @param ssoEnabled is service allowed to take part in SSO?
     */
    public TimeBasedRegisteredServiceAccessStrategy(final boolean enabled, final boolean ssoEnabled) {
        super(enabled, ssoEnabled);
    }

    public String getStartingDateTime() {
        return startingDateTime;
    }

    public String getEndingDateTime() {
        return endingDateTime;
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
                .append("startingDateTime", startingDateTime)
                .append("endingDateTime", endingDateTime)
                .toString();
    }


    @Override
    public boolean isServiceAccessAllowed() {
        final DateTime now = DateTime.now();

        if (this.startingDateTime != null) {
            final DateTime st = DateTime.parse(this.startingDateTime);

            if (now.isBefore(st)) {
                logger.warn("Service access not allowed because it starts at {}. Now is {}",
                        this.startingDateTime, now);
                return false;
            }
        }

        if (this.endingDateTime != null) {
            final DateTime et = DateTime.parse(this.endingDateTime);
            if  (now.isAfter(et)) {
                logger.warn("Service access not allowed because it ended at {}. Now is {}",
                        this.endingDateTime, now);
                return false;
            }
        }

        return super.isServiceAccessAllowed();
    }

}
