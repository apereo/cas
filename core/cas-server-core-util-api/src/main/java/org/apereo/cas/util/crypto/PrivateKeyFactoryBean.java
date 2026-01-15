package org.apereo.cas.util.crypto;

import module java.base;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

/**
 * Factory Bean for creating a private key from a file.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@Getter
@Setter
public class PrivateKeyFactoryBean extends AbstractFactoryBean<@NonNull PrivateKey> {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private Resource location;

    private String algorithm;

    @Override
    public Class getObjectType() {
        return PrivateKey.class;
    }

    @NonNull
    @Override
    protected PrivateKey createInstance() {
        var key = readPemPrivateKey();
        if (key == null) {
            LOGGER.debug("Key [{}] is not in PEM format. Trying next...", this.location);
            key = readDERPrivateKey();
        }
        return key;
    }

    private PrivateKey readPemPrivateKey() {
        LOGGER.trace("Attempting to read as PEM [{}]", this.location);
        try (val in = new InputStreamReader(this.location.getInputStream(), StandardCharsets.UTF_8);
             val br = new BufferedReader(in);
             val pp = new PEMParser(br)) {

            val object = pp.readObject();
            if (object instanceof final PrivateKeyInfo info) {
                return new JcaPEMKeyConverter().getPrivateKey(info);
            }
            if (object instanceof final PEMKeyPair pemKeyPair) {
                val kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
                return kp.getPrivate();
            }
        } catch (final Exception e) {
            LOGGER.debug("Unable to read key", e);
        }
        return null;
    }

    private PrivateKey readDERPrivateKey() {
        LOGGER.debug("Attempting to read key as DER [{}]", this.location);
        try (val privKey = this.location.getInputStream()) {
            val bytes = new byte[(int) this.location.contentLength()];
            privKey.read(bytes);
            val privSpec = new PKCS8EncodedKeySpec(bytes);
            val factory = KeyFactory.getInstance(this.algorithm);
            return factory.generatePrivate(privSpec);
        } catch (final Exception e) {
            LOGGER.debug("Unable to read key", e);
            return null;
        }
    }

}
