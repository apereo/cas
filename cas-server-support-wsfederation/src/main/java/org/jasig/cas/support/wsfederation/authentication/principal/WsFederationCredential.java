package org.jasig.cas.support.wsfederation.authentication.principal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jasig.cas.authentication.Credential;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * This class represents the basic elements of the WsFederation token.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public final class WsFederationCredential implements Credential {
    private final transient Logger logger = LoggerFactory.getLogger(WsFederationCredential.class);

    private String audience;
    private String authenticationMethod;
    private String id;
    private String issuer;
    private DateTime issuedOn;
    private DateTime notBefore;
    private DateTime notOnOrAfter;
    private DateTime retrievedOn;
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

    public DateTime getIssuedOn() {
        return this.issuedOn;
    }

    public void setIssuedOn(final DateTime issuedOn) {
        this.issuedOn = issuedOn;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public DateTime getNotBefore() {
        return this.notBefore;
    }

    public void setNotBefore(final DateTime notBefore) {
        this.notBefore = notBefore;
    }

    public DateTime getNotOnOrAfter() {
        return this.notOnOrAfter;
    }

    public void setNotOnOrAfter(final DateTime notOnOrAfter) {
        this.notOnOrAfter = notOnOrAfter;
    }

    public DateTime getRetrievedOn() {
        return this.retrievedOn;
    }

    public void setRetrievedOn(final DateTime retrievedOn) {
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
    public boolean isValid(final String expectedAudience, final String expectedIssuer, final int timeDrift) {
        if (!this.getAudience().equalsIgnoreCase(expectedAudience)) {
            logger.warn("Audience is invalid: {}", this.getAudience());
            return false;
        }

        if (!this.getIssuer().equalsIgnoreCase(expectedIssuer)) {
            logger.warn("Issuer is invalid: {}", this.getIssuer());
            return false;
        }

        final DateTime retrievedOnTimeDrift = this.getRetrievedOn().minusMillis(timeDrift);
        if (this.getIssuedOn().isBefore(retrievedOnTimeDrift)) {
            logger.warn("Ticket is issued before the allowed drift. Issued on {} while allowed drift is {}",
                    this.getIssuedOn(), retrievedOnTimeDrift);
            return false;
        }

        final DateTime retrievedOnTimeAfterDrift = this.getRetrievedOn().plusMillis(timeDrift);
        if (this.getIssuedOn().isAfter(retrievedOnTimeAfterDrift)) {
            logger.warn("Ticket is issued after the allowed drift. Issued on {} while allowed drift is {}",
                    this.getIssuedOn(), retrievedOnTimeAfterDrift);
            return false;
        }

        if (this.getRetrievedOn().isAfter(this.getNotOnOrAfter())) {
            logger.warn("Ticket is too late because it's retrieved on {} which is after {}.",
                    this.getRetrievedOn(), this.getNotOnOrAfter());
            return false;
        }

        logger.debug("WsFed Credential is validated for {} and {}.", expectedAudience, expectedIssuer);
        return true;
    }
}
