package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorProperties;

import lombok.val;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * An SSL utility class to use a client certificate.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public class SSLUtil {

    public static KeyManagerFactory buildKeystore(final InweboMultifactorProperties properties) throws Exception {
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        val keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream keyInput = properties.getClientCertificate().getLocation().getInputStream()) {
            keyStore.load(keyInput, properties.getCertificatePassphrase().toCharArray());

            keyInput.close();
            keyManagerFactory.init(keyStore, properties.getCertificatePassphrase().toCharArray());
        }
        return keyManagerFactory;
    }
}
