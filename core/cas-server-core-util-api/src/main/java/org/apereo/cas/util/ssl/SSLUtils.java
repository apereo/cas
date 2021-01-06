package org.apereo.cas.util.ssl;

import org.apereo.cas.configuration.model.core.util.ClientCertificateProperties;

import lombok.experimental.UtilityClass;
import lombok.val;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * An SSL utility class.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@UtilityClass
public class SSLUtils {

    public static KeyManagerFactory buildKeystore(final ClientCertificateProperties properties) throws Exception {
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        val keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream keyInput = properties.getCertificate().getLocation().getInputStream()) {
            keyStore.load(keyInput, properties.getPassphrase().toCharArray());

            keyInput.close();
            keyManagerFactory.init(keyStore, properties.getPassphrase().toCharArray());
        }
        return keyManagerFactory;
    }
}
