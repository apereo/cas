package org.apereo.cas.configuration.model.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

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
@Slf4j
@Getter
@Setter
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
}
