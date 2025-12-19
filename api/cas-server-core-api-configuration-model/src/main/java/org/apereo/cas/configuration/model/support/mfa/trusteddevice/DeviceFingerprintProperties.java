package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Device fingerprint configuration for MFA Trusted Devices.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-trusted-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class DeviceFingerprintProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 747021103142441353L;

    /**
     * Core settings for device fingerprinting.
     */
    private Core core = new Core();

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

    /**
     * Configure usage of geo-location within trusted device fingerprints.
     */
    private GeoLocation geolocation = new GeoLocation();

    /**
     * Configure usage of browser within trusted device fingerprints.
     */
    private GeoLocation browser = new GeoLocation();

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-trusted-mfa")
    public static class ClientIp extends BaseDeviceFingerprintComponentProperties {
        @Serial
        private static final long serialVersionUID = 785014133279201757L;

        public ClientIp() {
            setEnabled(true);
            setOrder(2);
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-trusted-mfa")
    public static class Cookie extends CookieProperties {
        @Serial
        private static final long serialVersionUID = -9022498833437602657L;

        /**
         * The default max age for the cookie component.
         */
        private static final Duration DEFAULT_MAX_AGE_DAYS = Duration.ofDays(30);

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
            setMaxAge(DEFAULT_MAX_AGE_DAYS.toString());

            crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
            crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-trusted-mfa")
    public static class UserAgent extends BaseDeviceFingerprintComponentProperties {
        @Serial
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

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-trusted-mfa")
    public static class GeoLocation extends BaseDeviceFingerprintComponentProperties {
        @Serial
        private static final long serialVersionUID = -4125531035180836136L;

        /**
         * Default Order for GeoLocation component.
         */
        private static final int DEFAULT_ORDER = 4;

        public GeoLocation() {
            setEnabled(false);
            setOrder(DEFAULT_ORDER);
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-trusted-mfa")
    public static class Browser extends BaseDeviceFingerprintComponentProperties {
        @Serial
        private static final long serialVersionUID = -4125531035180836136L;

        /**
         * Default Order for GeoLocation component.
         */
        private static final int DEFAULT_ORDER = 5;

        public Browser() {
            setEnabled(false);
            setOrder(DEFAULT_ORDER);
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-trusted-mfa")
    public static class Core implements Serializable {
        @Serial
        private static final long serialVersionUID = 3290828606869889754L;
        /**
         * Component Separator for device fingerprints.
         */
        private String componentSeparator = "@";
    }
}
