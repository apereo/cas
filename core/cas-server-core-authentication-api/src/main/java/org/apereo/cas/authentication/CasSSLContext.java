package org.apereo.cas.authentication;

import lombok.val;
import org.apache.http.ssl.SSLContexts;
import org.jooq.lambda.Unchecked;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

/**
 * This is {@link CasSSLContext}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface CasSSLContext {
    /**
     * System default ssl context, key and trust managers.
     *
     * @return the cas ssl context
     */
    static CasSSLContext system() {
        return new CasSSLContext() {
            @Override
            public SSLContext getSslContext() {
                return SSLContexts.createSystemDefault();
            }

            @Override
            public TrustManager[] getTrustManagers() {
                return Unchecked.supplier(() -> {
                    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    factory.init((KeyStore) null);
                    return factory.getTrustManagers();
                }).get();
            }

            @Override
            public KeyManager[] getKeyManagers() {
                return Unchecked.supplier(() -> {
                    val factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    factory.init(null, null);
                    return factory.getKeyManagers();
                }).get();
            }
        };
    }

    /**
     * Gets ssl context.
     *
     * @return the ssl context
     */
    SSLContext getSslContext();

    /**
     * Get trust managers.
     *
     * @return the trust manager [ ]
     */
    TrustManager[] getTrustManagers();

    /**
     * Get key managers.
     *
     * @return the key manager [ ]
     */
    KeyManager[] getKeyManagers();
}
