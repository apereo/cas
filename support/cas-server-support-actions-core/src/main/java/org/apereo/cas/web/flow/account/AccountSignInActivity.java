package org.apereo.cas.web.flow.account;

import module java.base;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link AccountSignInActivity}, a sign-in attempt recorded by the CAS event repository
 * as shown in the account profile.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@RequiredArgsConstructor
class AccountSignInActivity implements Serializable {
    /**
     * Event types shown as sign-in activity, mapped to their outcome.
     */
    static final Map<String, String> OUTCOMES = Map.of(
        CasTicketGrantingTicketCreatedEvent.class.getCanonicalName(), "success",
        CasAuthenticationTransactionFailureEvent.class.getCanonicalName(), "failure",
        CasAuthenticationPolicyFailureEvent.class.getCanonicalName(), "failure",
        CasRiskyAuthenticationDetectedEvent.class.getCanonicalName(), "risk");

    @Serial
    private static final long serialVersionUID = 2894529184707358473L;

    private final String outcome;

    private final ZonedDateTime when;

    private final @Nullable String clientIpAddress;

    private final @Nullable String location;

    private final @Nullable String userAgent;

    static AccountSignInActivity from(final CasEvent event) {
        val geoLocation = event.getGeoLocation();
        val location = StringUtils.isNotBlank(geoLocation.getAddress())
            ? geoLocation.getAddress()
            : StringUtils.isNotBlank(geoLocation.getLatitude()) && StringUtils.isNotBlank(geoLocation.getLongitude())
                ? geoLocation.getLatitude() + ", " + geoLocation.getLongitude()
                : null;
        return new AccountSignInActivity(OUTCOMES.getOrDefault(event.getType(), "success"),
            event.getCreationTime().atZone(ZoneOffset.UTC), event.getClientIpAddress(), location, event.getAgent());
    }
}
