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

    /**
     * A list of options to present to the client for available MFA Providers.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum Providers {
        /**
         * For mfa-duo.
         */
        MFA_DUO("mfa-duo", "Duo Security"),
        /**
         * For mfa-authy.
         */
        MFA_AUTHY("mfa-authy", "Authy Authenticator"),
        /**
         * For mfa-yubikey.
         */
        MFA_YUBIKEY("mfa-yubikey", "YubiKey"),
        /**
         * For mfa-radius.
         */
        MFA_RADIUS("mfa-radius", "RSA/RADIUS"),
        /**
         * For mfa-wikid.
         */
        MFA_WIKID("mfa-wikid", "WiKID"),
        /**
         * For mfa-gauth.
         */
        MFA_GAUTH("mfa-gauth", "Google Authenitcator"),
        /**
         * For mfa-azure.
         */
        MFA_AZURE("mfa-azure", "Microsoft Azure"),
        /**
         * For mfa-u2f.
         */
        MFA_U2F("mfa-u2f", "FIDO U2F"),
        /**
         * For mfa-swivel.
         */
        MFA_SWIVEL("mfa-swivel", "Swivel Secure");

        private final String value;
        private final String display;

        Providers(final String value, final String display) {
            this.value = value;
            this.display = display;
        }

        /**
         * Returns the display string for this property.
         *
         * @return - String to display
         */
        public String getDisplay() {
            return this.display;
        }

        /**
         * Returns the value to be stored for this property.
         *
         * @return - String value of the property
         */
        public String getValue() {
            return this.value;
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
