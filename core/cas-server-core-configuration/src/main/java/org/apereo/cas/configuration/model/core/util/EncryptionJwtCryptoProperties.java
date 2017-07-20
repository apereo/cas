package org.apereo.cas.configuration.model.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link EncryptionJwtCryptoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class EncryptionJwtCryptoProperties {

    /**
     * The encryption key is a JWT whose length is defined by the signing key size setting.
     */
    private String key = StringUtils.EMPTY;

    /**
     * The encryption key size.
     */
    private int keySize = 512;

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
