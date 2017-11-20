package org.apereo.cas.configuration.model.core.util;


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
public class EncryptionRandomizedSigningJwtCryptographyProperties implements Serializable {

    private static final long serialVersionUID = -6802876221525521736L;
    /**
     * Whether crypto operations are enabled.
     */
    private boolean enabled = true;

    /**
     * Settings that deal with encryption of values.
     */
    private EncryptionRandomizedCryptoProperties encryption = new EncryptionRandomizedCryptoProperties();

    /**
     * Settings that deal with signing of values.
     */
    private SigningJwtCryptoProperties signing = new SigningJwtCryptoProperties();

    /**
     * The signing/encryption algorithm to use.
     */
    private String alg = "AES";

    public EncryptionRandomizedCryptoProperties getEncryption() {
        return encryption;
    }

    public void setEncryption(final EncryptionRandomizedCryptoProperties encryption) {
        this.encryption = encryption;
    }

    public SigningJwtCryptoProperties getSigning() {
        return signing;
    }

    public void setSigning(final SigningJwtCryptoProperties signing) {
        this.signing = signing;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(final String alg) {
        this.alg = alg;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }


}
