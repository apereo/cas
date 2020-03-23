package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link SigningJwtCryptoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class SigningJwtCryptoProperties implements Serializable {

    private static final long serialVersionUID = -552544781333015532L;

    /**
     * The signing key is a JWT whose length is defined by the signing key size setting.
     */
    @RequiredProperty
    private String key = StringUtils.EMPTY;

    /**
     * The signing key size.
     */
    private int keySize = 512;
}
