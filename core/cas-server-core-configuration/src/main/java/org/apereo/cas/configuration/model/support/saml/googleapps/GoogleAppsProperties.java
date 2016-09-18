package org.apereo.cas.configuration.model.support.saml.googleapps;

/**
 * This is {@link GoogleAppsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class GoogleAppsProperties {

    private String publicKeyLocation = "file:/etc/cas/public.key";
    private String privateKeyLocation = "file:/etc/cas/private.key";
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
