package org.apereo.cas.util.crypto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * FactoryBean for creating a public key from a file.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@Slf4j
@ToString(callSuper = true)
public class PublicKeyFactoryBean extends AbstractFactoryBean<PublicKey> {

    private Resource resource;

    private String algorithm;

    @Override
    protected PublicKey createInstance() throws Exception {
        LOGGER.debug("Creating public key instance from [{}] using [{}]", this.resource.getFilename(), this.algorithm);
        try (InputStream pubKey = this.resource.getInputStream()) {
            final byte[] bytes = new byte[pubKey.available()];
            pubKey.read(bytes);
            final X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(bytes);
            final KeyFactory factory = KeyFactory.getInstance(this.algorithm);
            return factory.generatePublic(pubSpec);
        }
    }

    @Override
    public Class getObjectType() {
        return PublicKey.class;
    }

    public Resource getResource() {
        return this.resource;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setLocation(final Resource resource) {
        this.resource = resource;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }
}
