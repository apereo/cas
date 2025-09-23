package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwkCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcResponseProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcResponseProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 632228615694269271L;

    /**
     * Crypto settings for response mode JWTs, etc.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningOptionalJwkCryptographyProperties crypto =
        new EncryptionOptionalSigningOptionalJwkCryptographyProperties();

}
