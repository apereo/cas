package org.apereo.cas.util.ssl;

import org.apereo.cas.configuration.model.core.util.ClientCertificateProperties;
import org.apereo.cas.util.RandomUtils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;

/**
 * An SSL utility class.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@UtilityClass
public class SSLUtils {

    /**
     * Build keystore key manager factory.
     *
     * @param properties the properties
     * @return the key manager factory
     */
    @SneakyThrows
    public static KeyManagerFactory buildKeystore(final ClientCertificateProperties properties) {
        try (val keyInput = properties.getCertificate().getLocation().getInputStream()) {
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            val keyStore = KeyStore.getInstance("PKCS12");

            keyStore.load(keyInput, properties.getPassphrase().toCharArray());
            keyManagerFactory.init(keyStore, properties.getPassphrase().toCharArray());
            return keyManagerFactory;
        }
    }

    /**
     * Build ssl context.
     *
     * @param clientCertificate the client certificate
     * @return the ssl context
     */
    @SneakyThrows
    public static SSLContext buildSSLContext(final ClientCertificateProperties clientCertificate) {
        val keyManagerFactory = SSLUtils.buildKeystore(clientCertificate);
        val context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), null, RandomUtils.getNativeInstance());
        return context;
    }
}
