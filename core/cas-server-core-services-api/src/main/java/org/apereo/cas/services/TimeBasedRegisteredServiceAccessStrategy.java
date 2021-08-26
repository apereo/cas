package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
@EqualsAndHashCode(callSuper = true)
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TimeBasedRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -6180748828025837047L;

    @ExpressionLanguageCapable
    private String startingDateTime;

    @ExpressionLanguageCapable
    private String endingDateTime;

    @ExpressionLanguageCapable
    private String zoneId = ZoneOffset.UTC.getId();

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
        return doesStartingTimeAllowServiceAccess() && doesEndingTimeAllowServiceAccess() && super.isServiceAccessAllowed();
    }

    /**
     * Does ending time allow service access boolean.
     *
     * @return true/false
     */
    protected boolean doesEndingTimeAllowServiceAccess() {
        val endDateTime = getEndingDateTime();
        if (endDateTime != null) {
            val et = DateTimeUtils.zonedDateTimeOf(endDateTime);
            if (et != null) {
                val now = ZonedDateTime.now(ZoneId.of(getZoneId()));
                if (now.isAfter(et)) {
                    LOGGER.warn("Service access not allowed because it ended at [{}]. Now is [{}]", endDateTime, now);
                    return false;
                }
            } else {
                val etLocal = DateTimeUtils.localDateTimeOf(endDateTime);
                if (etLocal != null) {
                    val now = LocalDateTime.now(ZoneId.of(getZoneId()));
                    if (now.isAfter(etLocal)) {
                        LOGGER.warn("Service access not allowed because it ended at [{}]. Now is [{}]", endDateTime, now);
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
        val startDateTime = getStartingDateTime();
        if (startDateTime != null) {
            val st = DateTimeUtils.zonedDateTimeOf(startDateTime);
            if (st != null) {
                val now = ZonedDateTime.now(ZoneId.of(getZoneId()));
                if (now.isBefore(st)) {
                    LOGGER.warn("Service access not allowed because it starts at [{}]. Zoned now is [{}]", startDateTime, now);
                    return false;
                }
            } else {
                val stLocal = DateTimeUtils.localDateTimeOf(startDateTime);
                if (stLocal != null) {
                    val now = LocalDateTime.now(ZoneId.of(getZoneId()));
                    if (now.isBefore(stLocal)) {
                        LOGGER.warn("Service access not allowed because it starts at [{}]. Local now is [{}]", startDateTime, now);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String getStartingDateTime() {
        return StringUtils.isBlank(startingDateTime)
            ? null
            : SpringExpressionLanguageValueResolver.getInstance().resolve(startingDateTime);
    }

    public String getEndingDateTime() {
        return StringUtils.isBlank(endingDateTime)
            ? null
            : SpringExpressionLanguageValueResolver.getInstance().resolve(endingDateTime);
    }

    public String getZoneId() {
        return StringUtils.isBlank(zoneId)
            ? ZoneOffset.UTC.getId()
            : SpringExpressionLanguageValueResolver.getInstance().resolve(zoneId);
    }
}
