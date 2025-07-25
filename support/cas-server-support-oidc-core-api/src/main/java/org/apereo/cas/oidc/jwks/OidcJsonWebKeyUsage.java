package org.apereo.cas.oidc.jwks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.Use;

/**
 * This is {@link OidcJsonWebKeyUsage}.
 * The use member identifies the intended use of the key. Values
 * defined by this specification are {@code sig} (signature) and {@code sig} (encryption).
 * Other values MAY be used. The use value is case sensitive.
 * This member is OPTIONAL.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@RequiredArgsConstructor
public enum OidcJsonWebKeyUsage {
    /**
     * Indicate this key is used for encryption operations.
     */
    ENCRYPTION(Use.ENCRYPTION),
    /**
     * Indicate this key is used for signing operations.
     */
    SIGNING(Use.SIGNATURE);

    private final String value;

    /**
     * Assign usage to key.
     *
     * @param key the key
     */
    public void assignTo(final JsonWebKey key) {
        key.setUse(getValue());
    }

    /**
     * Is usage the same as this instance?
     *
     * @param use the use
     * @return true/false
     */
    public boolean is(final String use) {
        return StringUtils.isNotBlank(use) && Strings.CI.equals(use.trim(), getValue());
    }

    /**
     * Is key usage the same as this instance?
     *
     * @param key the key
     * @return true/false
     */
    public boolean is(final JsonWebKey key) {
        return is(key.getUse());
    }
}
