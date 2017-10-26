package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link RegisteredServiceMultifactorPolicy} that describes how a service
 * should handle authentication requests.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceMultifactorPolicy extends Serializable {

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum Providers {
        MFA_DUO("mfa-duo", "Duo Security"),
        MFA_AUTHY("mfa-authy","Authy Authenticator"),
        MFA_YUBIKEY("mfa-yubikey","YubiKey"),
        MFA_RADIUS("mfa-radius","RSA/RADIUS"),
        MFA_WIKID("mfa-wikid","WiKID"),
        MFA_GAUTH("mfa-gauth","Google Authenitcator"),
        MFA_AZURE("mfa-azure","Microsoft Azure"),
        MFA_U2F("mfa-u2f","FIDO U2F"),
        MFA_SWIVEL("mfa-swivel","Swivel Secure");

        private final String value;
        private final String display;

        Providers(final String value, final String display) {
            this.value = value;
            this.display = display;
        }

        public String getValue() {
            return value;
        }

        public String getDisplay() {
            return display;
        }
    }
    /**
     * The enum Failure modes.
     */
    enum FailureModes {
        /**
         * Disallow MFA, proceed with authentication but don't communicate MFA to the RP.
         */
        OPEN,
        /**
         * Disallow MFA, block with authentication.
         */
        CLOSED,
        /**
         * Disallow MFA, proceed with authentication and communicate MFA to the RP.
         */
        PHANTOM,

        /**
         * Do not check for failure at all.
         */
        NONE,

        /**
         * The default one indicating that no failure mode is set at all.
         */
        NOT_SET
    }

    /**
     * Gets MFA authentication provider id.
     *
     * @return the authentication provider id
     */
    Set<String> getMultifactorAuthenticationProviders();

    /**
     * Gets failure mode.
     *
     * @return the failure mode
     */
    FailureModes getFailureMode();

    /**
     * Gets principal attribute name trigger.
     *
     * @return the principal attribute name trigger
     */
    String getPrincipalAttributeNameTrigger();

    /**
     * Gets principal attribute value to match.
     * Values may be regex patterns.
     *
     * @return the principal attribute value to match
     */
    String getPrincipalAttributeValueToMatch();

    /**
     * Indicates whether authentication should be skipped.
     *
     * @return true/false
     */
    boolean isBypassEnabled();

}
