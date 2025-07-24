package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link EncryptionJwtCryptoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class EncryptionJwtCryptoProperties implements Serializable {

    /**
     * The default content encryption algorithm.
     */
    public static final String DEFAULT_CONTENT_ENCRYPTION_ALGORITHM =
        ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512;

    /**
     * Encryption key size for text data and ciphers.
     */
    public static final int DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE = 512;

    @Serial
    private static final long serialVersionUID = 616825635591169628L;

    /**
     * The encryption key is a string whose length is defined by the encryption key size setting.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String key = StringUtils.EMPTY;

    /**
     * The encryption key size.
     */
    private int keySize = DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE;
}
