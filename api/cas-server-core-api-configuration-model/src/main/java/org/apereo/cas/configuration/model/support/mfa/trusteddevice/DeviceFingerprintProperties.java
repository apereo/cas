package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.time.Duration;

/**
 * Device fingerprint configuration for MFA Trusted Devices.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-trusted-mfa")
@Getter
@Setter
public class DeviceFingerprintProperties implements Serializable {
    private static final long serialVersionUID = 747021103142441353L;

    /**
     * Component Separator for device fingerprints.
     */
    private String componentSeparator = "@";

    /**
     * Configure usage of client ip within trusted device fingerprints.
     */
    private ClientIp clientIp = new ClientIp();

    /**
     * Configure usage of a device cookie within trusted device fingerprints.
     */
    private Cookie cookie = new Cookie();

    /**
     * Configure usage of User-Agent header within trusted device fingerprints.
     */
    private UserAgent userAgent = new UserAgent();

    public static class ClientIp extends BaseDeviceFingerprintComponentProperties {
        private static final long serialVersionUID = 785014133279201757L;

        public ClientIp() {
            setEnabled(true);
            setOrder(2);
        }
    }

    @Getter
    @Setter
    public static class Cookie extends CookieProperties {
        private static final long serialVersionUID = -9022498833437602657L;

        /**
         * The default max age for the cookie component.
         */
        private static final int DEFAULT_MAX_AGE_DAYS = 30;

        /**
         * Is this component enabled or not.
         */
        private boolean enabled = true;

        /**
         * Indicates the order of components when generating a device fingerprint.
         */
        private int order = 1;

        /**
         * Crypto settings that sign/encrypt the cookie value stored on the client machine.
         */
        @NestedConfigurationProperty
        private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

        public Cookie() {
            setName("MFATRUSTED");
            setMaxAge((int) Duration.ofDays(DEFAULT_MAX_AGE_DAYS).getSeconds());
        }
    }

    public static class UserAgent extends BaseDeviceFingerprintComponentProperties {
        private static final long serialVersionUID = -5325531035180836136L;

        /**
         * Default Order for UserAgent component.
         */
        private static final int DEFAULT_ORDER = 3;

        public UserAgent() {
            setEnabled(false);
            setOrder(DEFAULT_ORDER);
        }
    }
}
