package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link CoreGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth")
@Getter
@Setter
@Accessors(chain = true)
public class CoreGoogleAuthenticatorMultifactorProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -7451748853833491119L;

    /**
     * Issuer used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    @RequiredProperty
    private String issuer = "CASIssuer";

    /**
     * Label used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    @RequiredProperty
    private String label = "CASLabel";

    /**
     * Length of the generated code.
     */
    private int codeDigits = 6;

    /**
     * The expiration time of the generated code in seconds.
     */
    private long timeStepSize = 30;

    /**
     * Since TOTP passwords are time-based, it is essential that the clock of both the server and
     * the client are synchronised within
     * the tolerance defined here as the window size.
     */
    private int windowSize = 3;

    /**
     * Maximum number of authentication attempts allowed
     * for a token validation attempt. If the number of attempts
     * exceeds this value, authentication will halt.
     * A negative or zero value (default) means no limits are enforced.
     * Note that the user account is not locked out by default; only the
     * CAS authentication flow is halted and user is notified and required
     * to restart the authentication process again.
     */
    private int maximumAuthenticationAttempts;

    /**
     * When enabled, allows the user/system to accept multiple accounts
     * and device registrations per user, allowing one to switch between
     * or register new devices/accounts automatically.
     */
    private boolean multipleDeviceRegistrationEnabled;

    /**
     * When enabled, allows the user/system to register accounts
     * and devices.
     */
    private boolean deviceRegistrationEnabled = true;

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Scratch code settings.
     */
    @NestedConfigurationProperty
    private GoogleAuthenticatorMultifactorScratchCodeProperties scratchCodes =
        new GoogleAuthenticatorMultifactorScratchCodeProperties();
}
