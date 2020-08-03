package org.apereo.cas.support.wsfederation.authentication.principal;

import org.apereo.cas.authentication.Credential;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * This class represents the basic elements of the WsFederation token.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Slf4j
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WsFederationCredential implements Credential {

    private static final long serialVersionUID = -824605020472810939L;

    private String audience;

    private String authenticationMethod;

    private String id;

    private String issuer;

    private ZonedDateTime issuedOn;

    private ZonedDateTime notBefore;

    private ZonedDateTime notOnOrAfter;

    private ZonedDateTime retrievedOn;

    private Map<String, List<Object>> attributes;

    /**
     * Validates the credential.
     *
     * @param expectedAudience the audience that the token was issued to (CAS Server)
     * @param expectedIssuer   the issuer of the token (the IdP)
     * @param timeDrift        the amount of acceptable time drift
     * @return true if the credentials are valid, otherwise false
     */
    public boolean isValid(final String expectedAudience, final String expectedIssuer, final long timeDrift) {
        if (!this.audience.equalsIgnoreCase(expectedAudience)) {
            LOGGER.warn("Audience [{}] is invalid where the expected audience should be [{}]", this.audience, expectedAudience);
            return false;
        }
        if (!this.issuer.equalsIgnoreCase(expectedIssuer)) {
            LOGGER.warn("Issuer [{}] is invalid since the expected issuer should be [{}]", this.issuer, expectedIssuer);
            return false;
        }
        val retrievedOnTimeDrift = getRetrievedOn().minus(timeDrift, ChronoUnit.MILLIS);
        if (getIssuedOn().isBefore(retrievedOnTimeDrift)) {
            LOGGER.warn("Ticket is issued before the allowed drift. Issued on [{}] while allowed drift is [{}]", this.issuedOn, retrievedOnTimeDrift);
            return false;
        }
        val retrievedOnTimeAfterDrift = getRetrievedOn().plus(timeDrift, ChronoUnit.MILLIS);
        if (getIssuedOn().isAfter(retrievedOnTimeAfterDrift)) {
            LOGGER.warn("Ticket is issued after the allowed drift. Issued on [{}] while allowed drift is [{}]", getIssuedOn(), retrievedOnTimeAfterDrift);
            return false;
        }
        if (getRetrievedOn().isAfter(this.notOnOrAfter)) {
            LOGGER.warn("Ticket is too late because it's retrieved on [{}] which is after [{}].", getRetrievedOn(), this.notOnOrAfter);
            return false;
        }
        LOGGER.debug("WsFed Credential is validated for [{}] and [{}].", expectedAudience, expectedIssuer);
        return true;
    }
}
