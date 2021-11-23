package org.apereo.cas.configuration.model.support.oidc.jwks;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OidcJsonWebKeystoreCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcJsonWebKeystoreCoreProperties")
public class OidcJsonWebKeystoreCoreProperties implements Serializable {
    private static final long serialVersionUID = -2696060572027445151L;

    /**
     * Timeout that indicates how long should the JWKS file be kept in cache.
     */
    @DurationCapable
    private String jwksCacheExpiration = "PT60M";

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
     * The key identifier to set for the generated key in the keystore.
     */
    private String jwksKeyId = "cas";
}
