package org.apereo.cas.configuration.model.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link SigningJwtCryptoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
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
