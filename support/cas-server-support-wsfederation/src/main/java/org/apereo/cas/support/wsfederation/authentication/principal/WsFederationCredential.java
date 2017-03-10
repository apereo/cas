package org.apereo.cas.support.wsfederation.authentication.principal;

import org.apereo.cas.authentication.Credential;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class WsFederationCredential implements Credential {
    private static final long serialVersionUID = -824605020472810939L;
    private static final Logger LOGGER = LoggerFactory.getLogger(WsFederationCredential.class);

    private String audience;
    private String authenticationMethod;
    private String id;
    private String issuer;
    private ZonedDateTime issuedOn;
    private ZonedDateTime notBefore;
    private ZonedDateTime notOnOrAfter;
    private ZonedDateTime retrievedOn;
    private Map<String, List<Object>> attributes;

    public String getAuthenticationMethod() {
        return this.authenticationMethod;
    }

    public void setAuthenticationMethod(final String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getAudience() {
        return this.audience;
    }

    public void setAudience(final String audience) {
        this.audience = audience;
    }

    public Map<String, List<Object>> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(final Map<String, List<Object>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public ZonedDateTime getIssuedOn() {
        return this.issuedOn;
    }

    public void setIssuedOn(final ZonedDateTime issuedOn) {
        this.issuedOn = issuedOn;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public ZonedDateTime getNotBefore() {
        return this.notBefore;
    }

    public void setNotBefore(final ZonedDateTime notBefore) {
        this.notBefore = notBefore;
    }

    public ZonedDateTime getNotOnOrAfter() {
        return this.notOnOrAfter;
    }

    public void setNotOnOrAfter(final ZonedDateTime notOnOrAfter) {
        this.notOnOrAfter = notOnOrAfter;
    }

    public ZonedDateTime getRetrievedOn() {
        return this.retrievedOn;
    }

    public void setRetrievedOn(final ZonedDateTime retrievedOn) {
        this.retrievedOn = retrievedOn;
    }

    /**
     * toString produces a human readable representation of the WsFederationCredential.
     *
     * @return a human readable representation of the WsFederationCredential
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("ID", this.id)
                .append("Issuer", this.issuer)
                .append("Audience", this.audience)
                .append("Authentication Method", this.authenticationMethod)
                .append("Issued On", this.issuedOn)
                .append("Valid After", this.notBefore)
                .append("Valid Before", this.notOnOrAfter)
                .append("Attributes", this.attributes)
                .build();

    }

    /**
     * isValid validates the credential.
     *
     * @param expectedAudience the audience that the token was issued to (CAS Server)
     * @param expectedIssuer   the issuer of the token (the IdP)
     * @param timeDrift        the amount of acceptable time drift
     * @return true if the credentials are valid, otherwise false
     */
    public boolean isValid(final String expectedAudience, final String expectedIssuer, final long timeDrift) {
        if (!this.getAudience().equalsIgnoreCase(expectedAudience)) {
            LOGGER.warn("Audience is invalid: [{}]", this.getAudience());
            return false;
        }

        if (!this.issuer.equalsIgnoreCase(expectedIssuer)) {
            LOGGER.warn("Issuer is invalid: [{}]", this.issuer);
            return false;
        }

        final ZonedDateTime retrievedOnTimeDrift = this.getRetrievedOn().minus(timeDrift, ChronoUnit.MILLIS);
        if (this.issuedOn.isBefore(retrievedOnTimeDrift)) {
            LOGGER.warn("Ticket is issued before the allowed drift. Issued on [{}] while allowed drift is [{}]",
                    this.issuedOn, retrievedOnTimeDrift);
            return false;
        }

        final ZonedDateTime retrievedOnTimeAfterDrift = this.retrievedOn.plus(timeDrift, ChronoUnit.MILLIS);
        if (this.issuedOn.isAfter(retrievedOnTimeAfterDrift)) {
            LOGGER.warn("Ticket is issued after the allowed drift. Issued on [{}] while allowed drift is [{}]",
                    this.issuedOn, retrievedOnTimeAfterDrift);
            return false;
        }

        if (this.retrievedOn.isAfter(this.notOnOrAfter)) {
            LOGGER.warn("Ticket is too late because it's retrieved on [{}] which is after [{}].",
                    this.retrievedOn, this.notOnOrAfter);
            return false;
        }

        LOGGER.debug("WsFed Credential is validated for [{}] and [{}].", expectedAudience, expectedIssuer);
        return true;
    }
}
