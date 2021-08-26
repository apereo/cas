package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link TrustedDevicesMultifactorCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-trusted-mfa")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("TrustedDevicesMultifactorCoreProperties")
public class TrustedDevicesMultifactorCoreProperties implements Serializable {

    private static final long serialVersionUID = 1585013239016790473L;

    /**
     * If an MFA request is bypassed due to a trusted authentication decision, applications will
     * receive a special attribute as part of the validation payload that indicates this behavior.
     * Applications must further account for the scenario where they ask for an MFA mode and
     * yet don’t receive confirmation of it in the response given the authentication
     * session was trusted and MFA bypassed.
     */
    private String authenticationContextAttribute = "isFromTrustedMultifactorAuthentication";

    /**
     * Indicates whether CAS should ask for device registration consent
     * or execute it automatically.
     */
    private boolean deviceRegistrationEnabled = true;

    /**
     * When device registration is enabled, indicate whether
     * a device name should be automatically selected and assigned by CAS.
     */
    private boolean autoAssignDeviceName;

    /**
     * Indicates how record keys for trusted devices would be generated
     * so they can be signed/verified on fetch operations.
     */
    private TrustedDevicesKeyGeneratorTypes keyGeneratorType = TrustedDevicesKeyGeneratorTypes.DEFAULT;

    public enum TrustedDevicesKeyGeneratorTypes {
        /**
         * Uses a combination of the username, device name and device fingerprint to generate the device key.
         */
        DEFAULT,
        /**
         * Deprecated. Uses a combination of the username, record date and device fingerprint to generate the device key.
         * @deprecated since 6.3.0
         */
        @Deprecated(since = "6.3.0")
        LEGACY
    }
}
