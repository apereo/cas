package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

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

    private static final long serialVersionUID = 616825635591169628L;

    /**
     * The encryption key is a JWT whose length is defined by the signing key size setting.
     */
    @RequiredProperty
    private String key = StringUtils.EMPTY;

    /**
     * The encryption key size.
     */
    private int keySize = 512;
}
