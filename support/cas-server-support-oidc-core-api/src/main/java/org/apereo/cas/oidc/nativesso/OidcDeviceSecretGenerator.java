package org.apereo.cas.oidc.nativesso;

/**
 * This is {@link OidcDeviceSecretGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public interface OidcDeviceSecretGenerator {
    /**
     * Bean name.
     */
    String BEAN_NAME = "oidcDeviceSecretGenerator";

    /**
     * Hash device secret string.
     *
     * @param deviceSecret the device secret
     * @return the string
     */
    String hash(String deviceSecret);

    /**
     * Generate device secret.
     *
     * @return the string
     */
    String generate();
}
