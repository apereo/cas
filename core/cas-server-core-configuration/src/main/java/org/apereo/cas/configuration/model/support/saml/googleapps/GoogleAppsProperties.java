package org.apereo.cas.configuration.model.support.saml.googleapps;

import java.io.Serializable;

/**
 * This is {@link GoogleAppsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class GoogleAppsProperties implements Serializable {

    private static final long serialVersionUID = -5133482766495375325L;
    /**
     * The public key location that is also shared with google apps.
     */
    private String publicKeyLocation = "file:/etc/cas/public.key";
    /**
     * The private key location that is used to sign responses, etc.
     */
    private String privateKeyLocation = "file:/etc/cas/private.key";

    /**
     * Signature algorithm used to generate keys.
     */
    private String keyAlgorithm = "RSA";

    public String getPublicKeyLocation() {
        return publicKeyLocation;
    }

    public void setPublicKeyLocation(final String publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public String getPrivateKeyLocation() {
        return privateKeyLocation;
    }

    public void setPrivateKeyLocation(final String privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(final String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }
}
