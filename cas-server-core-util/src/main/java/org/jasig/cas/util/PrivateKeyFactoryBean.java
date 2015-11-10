package org.jasig.cas.util;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;

/**
 * Factory Bean for creating a private key from a file.
 *
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public final class PrivateKeyFactoryBean extends AbstractFactoryBean<PrivateKey> {

    @NotNull
    private Resource location;

    @NotNull
    private String algorithm;

    @Override
    protected PrivateKey createInstance() throws Exception {
        try (final InputStream privKey = this.location.getInputStream()) {
            final byte[] bytes = new byte[privKey.available()];
            privKey.read(bytes);
            final PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(bytes);
            final KeyFactory factory = KeyFactory.getInstance(this.algorithm);
            return factory.generatePrivate(privSpec);
        }
    }

    @Override
    public Class getObjectType() {
        return PrivateKey.class;
    }

    public void setLocation(final Resource location) {
        this.location = location;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public Resource getLocation() {
        return location;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
