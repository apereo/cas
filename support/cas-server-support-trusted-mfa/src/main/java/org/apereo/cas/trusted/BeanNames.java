package org.apereo.cas.trusted;

import lombok.experimental.UtilityClass;
import org.apereo.cas.CipherExecutor;

/**
 * A utility class containing the names of several beans trusted-mfa provides.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@UtilityClass
public class BeanNames {
    public static final String BEAN_DEVICE_FINGERPRINT_STRATEGY = "deviceFingerprintStrategy";

    /**
     * The {@link CipherExecutor} used to sign & encrypt a cookie MFA trusted device fingerprint component.
     */
    public static final String BEAN_COOKIE_DEVICE_FINGERPRINT_COMPONENT_CIPHER_EXECUTOR =
            "cookieDeviceFingerprintComponentCipherExecutor";
}
