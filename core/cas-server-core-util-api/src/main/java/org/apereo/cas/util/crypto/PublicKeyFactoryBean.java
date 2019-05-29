package org.apereo.cas.util.crypto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
@Getter
@Setter
public class PublicKeyFactoryBean extends AbstractFactoryBean<PublicKey> {

    private Resource resource;

    private String algorithm;

    @Override
    protected PublicKey createInstance() throws Exception {
        var key = readPemPublicKey();
        if (key == null) {
            LOGGER.debug("Key [{}] is not in PEM format. Trying next...", this.resource);
            key = readDERPublicKey();
        }
        return key;
    }

    private PublicKey readPemPublicKey() throws Exception {
        try (val reader = new PemReader(new InputStreamReader(this.resource.getInputStream(), StandardCharsets.UTF_8))) {
            val pemObject = reader.readPemObject();
            val content = pemObject.getContent();
            val pubSpec = new X509EncodedKeySpec(content);
            val factory = KeyFactory.getInstance(this.algorithm);
            return factory.generatePublic(pubSpec);
        }
    }

    private PublicKey readDERPublicKey() throws Exception {
        LOGGER.debug("Creating public key instance from [{}] using [{}]", this.resource.getFilename(), this.algorithm);
        try (val pubKey = this.resource.getInputStream()) {
            val bytes = new byte[(int) this.resource.contentLength()];
            pubKey.read(bytes);
            val pubSpec = new X509EncodedKeySpec(bytes);
            val factory = KeyFactory.getInstance(this.algorithm);
            return factory.generatePublic(pubSpec);
        }
    }

    @Override
    public Class getObjectType() {
        return PublicKey.class;
    }
}
