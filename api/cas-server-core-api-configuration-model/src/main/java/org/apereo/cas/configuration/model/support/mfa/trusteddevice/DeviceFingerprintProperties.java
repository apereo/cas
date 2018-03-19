package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

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
            super(true, 0);
        }
    }

    @Getter
    @Setter
    public static class Cookie extends BaseDeviceFingerprintComponentProperties {
        private static final long serialVersionUID = -9022498833437602657L;

        /**
         * Crypto settings that sign/encrypt the cookie value stored on the client machine.
         */
        @NestedConfigurationProperty
        private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

        public Cookie() {
            super(false, 0);
        }
    }

    public static class UserAgent extends BaseDeviceFingerprintComponentProperties {
        private static final long serialVersionUID = -5325531035180836136L;

        public UserAgent() {
            super(true, 0);
        }
    }
}
