package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OidcJsonWebKeystoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcJsonWebKeystoreProperties")
public class OidcJsonWebKeystoreProperties implements Serializable {
    private static final long serialVersionUID = -1696060572027445151L;

    /**
     * Timeout that indicates how long should the JWKS file be kept in cache.
     */
    private int jwksCacheInMinutes = 60;

    /**
     * Path to the JWKS file resource used to handle signing/encryption of authentication tokens.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String jwksFile = "file:/etc/cas/config/keystore.jwks";

    /**
     * The key size for the generated jwks. This is an algorithm-specific metric,
     * such as modulus length, specified in number of bits.
     * <p>
     * If the keystore type is {@code EC}, the key size defined here
     * should switch to one of {@code 256}, {@code 384} or {@code 521}.
     * If using  {@code EC}, then the size should match the number of bits required.
     */
    private int jwksKeySize = 2048;

    /**
     * The type of the JWKS used to handle signing/encryption of authentication tokens.
     * Accepted values are {@code RSA} or {@code EC}.
     */
    private String jwksType = "RSA";

    /**
     * Fetch JWKS via a REST endpoint.
     */
    @NestedConfigurationProperty
    private RestfulOidcJsonWebKeystoreProperties rest = new RestfulOidcJsonWebKeystoreProperties();

}
