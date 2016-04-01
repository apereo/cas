package org.jasig.cas.util;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;

/**
 * FactoryBean for creating a public key from a file.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
public class PublicKeyFactoryBean extends AbstractFactoryBean<PublicKey> {
    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private Resource resource;

    @NotNull
    private String algorithm;

    @Override
    protected final PublicKey createInstance() throws Exception {
        logger.debug("Creating public key instance from [{}] using [{}]",
                this.resource.getFilename(), this.algorithm);

        try (final InputStream pubKey = this.resource.getInputStream()) {
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
        return resource;
    }


    public String getAlgorithm() {
        return algorithm;
    }

    public void setLocation(final Resource resource) {
        this.resource = resource;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("resource", this.resource)
                .append("algorithm", this.algorithm)
                .toString();
    }
}
