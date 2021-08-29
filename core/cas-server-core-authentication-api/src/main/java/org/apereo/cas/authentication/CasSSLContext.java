package org.apereo.cas.authentication;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContexts;
import org.jooq.lambda.Unchecked;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

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
            public HostnameVerifier getHostnameVerifier() {
                return new DefaultHostnameVerifier();
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
     * Disabled.
     *
     * @return the cas ssl context
     */
    static CasSSLContext disabled() {
        return new DisabledCasSslContext();
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

    /**
     * Gets hostname verifier.
     *
     * @return the hostname verifier
     */
    HostnameVerifier getHostnameVerifier();

    class DisabledCasSslContext implements CasSSLContext {
        private static X509TrustManager getDisabledTrustedManager() {
            return new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                }
            };
        }

        @Override
        @SneakyThrows
        public SSLContext getSslContext() {
            val sc = SSLContext.getInstance("SSL");
            sc.init(getKeyManagers(), getTrustManagers(), null);
            return sc;
        }

        @Override
        public TrustManager[] getTrustManagers() {
            return new TrustManager[]{getDisabledTrustedManager()};
        }

        @Override
        public KeyManager[] getKeyManagers() {
            return new KeyManager[0];
        }

        @Override
        public HostnameVerifier getHostnameVerifier() {
            return NoopHostnameVerifier.INSTANCE;
        }
    }
}
