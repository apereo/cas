package org.apereo.cas.services;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.DateTimeUtils;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.Getter;

/**
 * The {@link TimeBasedRegisteredServiceAccessStrategy} is responsible for
 * enforcing CAS authorization strategy based on a configured start/end time.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@Setter
@NoArgsConstructor
public class TimeBasedRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -6180748828025837047L;

    private String startingDateTime;

    private String endingDateTime;

    /**
     * Initiates the time-based access strategy.
     *
     * @param enabled    is service access allowed?
     * @param ssoEnabled is service allowed to take part in SSO?
     */
    public TimeBasedRegisteredServiceAccessStrategy(final boolean enabled, final boolean ssoEnabled) {
        super(enabled, ssoEnabled);
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
