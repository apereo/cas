package org.apereo.cas.trusted;

import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

import lombok.experimental.UtilityClass;

/**
 * A utility class containing the names of several beans trusted-mfa provides.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@UtilityClass
public class BeanNames {
    /**
     * The {@link DeviceFingerprintStrategy} used by the MFA trusted device module.
     */
    public static final String BEAN_DEVICE_FINGERPRINT_STRATEGY = "deviceFingerprintStrategy";

    /**
     * The {@link CipherExecutor} used to sign &amp; encrypt a MFA trusted device fingerprint cookie component.
     */
    public static final String BEAN_DEVICE_FINGERPRINT_COOKIE_CIPHER_EXECUTOR = "deviceFingerprintCookieCipherExecutor";

    /**
     * The {@link CookieRetrievingCookieGenerator} used to generate MFA trusted device fingerprint cookies.
     */
    public static final String BEAN_DEVICE_FINGERPRINT_COOKIE_GENERATOR = "deviceFingerprintCookieGenerator";

    /**
     * The {@link RandomStringGenerator} used to generate device fingerprint cookie values.
     */
    public static final String BEAN_DEVICE_FINGERPRINT_COOKIE_RANDOM_STRING_GENERATOR = "deviceFingerprintCookieRandomStringGenerator";
}
