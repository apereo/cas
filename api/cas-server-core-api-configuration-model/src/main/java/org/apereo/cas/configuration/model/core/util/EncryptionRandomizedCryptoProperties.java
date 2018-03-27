package org.apereo.cas.configuration.model.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link EncryptionRandomizedCryptoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class EncryptionRandomizedCryptoProperties implements Serializable {

    private static final long serialVersionUID = -6945916782426505112L;

    /**
     * The encryption key.
     * The encryption key by default and unless specified otherwise
     * must be randomly-generated string whose length
     * is defined by the encryption key size setting.
     */
    @RequiredProperty
    private String key = StringUtils.EMPTY;

    /**
     * Encryption key size.
     */
    private int keySize = 16;
}
