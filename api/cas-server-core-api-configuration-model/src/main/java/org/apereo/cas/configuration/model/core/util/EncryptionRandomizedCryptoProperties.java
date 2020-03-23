package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link EncryptionRandomizedCryptoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-core-util", automated = true)
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
