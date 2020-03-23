package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Common properties for all cryptography related configs.
 * A number of components in CAS accept signing and encryption keys.
 * In most scenarios if keys are not provided, CAS will auto-generate them.
 * The following instructions apply if you wish to manually and beforehand
 * create the signing and encryption keys.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class EncryptionJwtSigningJwtCryptographyProperties implements Serializable {

    private static final long serialVersionUID = -3015641631298039059L;

    /**
     * Whether crypto operations are enabled.
     */
    private boolean enabled = true;

    /**
     * Settings that deal with encryption of values.
     */
    private EncryptionJwtCryptoProperties encryption = new EncryptionJwtCryptoProperties();

    /**
     * Settings that deal with signing of values.
     */
    private SigningJwtCryptoProperties signing = new SigningJwtCryptoProperties();

    /**
     * The signing/encryption algorithm to use.
     */
    private String alg = CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM;

    /**
     * Control the cipher sequence of operations.
     * The accepted values are:
     * <ul>
     *     <li>{@code ENCRYPT_AND_SIGN}: Encrypt the value first, and then sign.</li>
     *     <li>{@code SIGN_AND_ENCRYPT}: Sign the value first, and then encrypt.</li>
     * </ul>
     */
    private String strategyType = "ENCRYPT_AND_SIGN";
}
