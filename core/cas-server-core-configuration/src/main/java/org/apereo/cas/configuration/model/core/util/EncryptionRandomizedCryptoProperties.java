package org.apereo.cas.configuration.model.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link EncryptionRandomizedCryptoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class EncryptionRandomizedCryptoProperties {
    /**
     * The encryption key.
     * The encryption key by default and unless specified otherwise
     * must be randomly-generated string whose length
     * is defined by the encryption key size setting.
     */
    private String key = StringUtils.EMPTY;
    /**
     * Encryption key size.
     */
    private int keySize = 16;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(final int keySize) {
        this.keySize = keySize;
    }
}
