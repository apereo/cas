package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration properties class for remote.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-generic-remote-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class RemoteAuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 573409035023089696L;

    /**
     * The authorized network address to allow for authentication.
     * This approach allows for transparent authentication achieved within a large corporate
     * network without the need to manage certificates, etc.
     */
    @RequiredProperty
    private String ipAddressRange = StringUtils.EMPTY;

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;

    /**
     * Remote cookie authentication settings.
     */
    private RemoteCookie cookie = new RemoteCookie();

    @RequiresModule(name = "cas-server-support-generic-remote-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class RemoteCookie implements Serializable {

        @Serial
        private static final long serialVersionUID = 1727479242798310605L;

        /**
         * The name of the remote cookie that is passed onto CAS,
         * usually set by a trusted third party. The cookie, when verified and decrypted,
         * must indicate the trusted remote principal id that CAS should use for authentication.
         */
        private String cookieName;

        /**
         * Crypto settings that determine how the cookie should be signed and encrypted.
         */
        @NestedConfigurationProperty
        private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

        RemoteCookie() {
            crypto.setEnabled(true);
            crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
            crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        }
    }
}
