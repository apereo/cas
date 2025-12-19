package org.apereo.cas.configuration.model.core.util;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link EncryptionOptionalSigningOptionalJwkCryptographyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-core-util", automated = true)
public class EncryptionOptionalSigningOptionalJwkCryptographyProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7285404480671258520L;

    /**
     * Whether crypto operations are enabled.
     */
    private boolean enabled = true;

    /**
     * Whether crypto encryption operations are enabled.
     */
    private boolean encryptionEnabled = true;

    /**
     * Whether crypto signing operations are enabled.
     */
    private boolean signingEnabled = true;

    /**
     * Control the cipher sequence of operations.
     * The accepted values are:
     * <ul>
     *     <li>{@code ENCRYPT_AND_SIGN}: Encrypt the value first, and then sign.</li>
     *     <li>{@code SIGN_AND_ENCRYPT}: Sign the value first, and then encrypt.</li>
     * </ul>
     */
    private String strategyType = "SIGN_AND_ENCRYPT";
}
