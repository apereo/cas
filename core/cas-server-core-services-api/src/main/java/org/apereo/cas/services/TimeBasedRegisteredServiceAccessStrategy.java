package org.apereo.cas.services;

import org.apereo.cas.util.DateTimeUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        return doesEndingTimeAllowServiceAccess() && super.isServiceAccessAllowed();
    }

    /**
     * Does ending time allow service access boolean.
     *
     * @return true/false
     */
    protected boolean doesEndingTimeAllowServiceAccess() {
        if (this.endingDateTime != null) {
            val et = DateTimeUtils.zonedDateTimeOf(this.endingDateTime);
            if (et != null) {
                val now = ZonedDateTime.now(ZoneOffset.UTC);
                if (now.isAfter(et)) {
                    LOGGER.warn("Service access not allowed because it ended at [{}]. Now is [{}]", this.endingDateTime, now);
                    return false;
                }
            } else {
                val etLocal = DateTimeUtils.localDateTimeOf(this.endingDateTime);
                if (etLocal != null) {
                    val now = LocalDateTime.now(ZoneOffset.UTC);
                    if (now.isAfter(etLocal)) {
                        LOGGER.warn("Service access not allowed because it ended at [{}]. Now is [{}]", this.endingDateTime, now);
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
            val st = DateTimeUtils.zonedDateTimeOf(this.startingDateTime);
            if (st != null) {
                val now = ZonedDateTime.now(ZoneOffset.UTC);
                if (now.isBefore(st)) {
                    LOGGER.warn("Service access not allowed because it starts at [{}]. Zoned now is [{}]", this.startingDateTime, now);
                    return false;
                }
            } else {
                val stLocal = DateTimeUtils.localDateTimeOf(this.startingDateTime);
                if (stLocal != null) {
                    val now = LocalDateTime.now(ZoneOffset.UTC);
                    if (now.isBefore(stLocal)) {
                        LOGGER.warn("Service access not allowed because it starts at [{}]. Local now is [{}]", this.startingDateTime, now);
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
