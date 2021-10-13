package org.apereo.cas.authentication;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContexts;
import org.jooq.lambda.Unchecked;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.Provider;
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
            public TrustManagerFactory getTrustManagerFactory() {
                return Unchecked.supplier(() -> {
                    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    factory.init((KeyStore) null);
                    return factory;
                }).get();
            }

            @Override
            public TrustManager[] getTrustManagers() {
                return getTrustManagerFactory().getTrustManagers();
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

    /**
     * Gets trust manager factory.
     *
     * @return the trust manager factory
     */
    TrustManagerFactory getTrustManagerFactory();

    class DisabledCasSslContext implements CasSSLContext {
        private static final X509Certificate[] ACCEPTED_ISSUERS = {};

        private static X509TrustManager getDisabledTrustedManager() {
            return new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return ACCEPTED_ISSUERS;
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
        public TrustManagerFactory getTrustManagerFactory() {
            val provider = new Provider(StringUtils.EMPTY, "0.0", StringUtils.EMPTY) {
                private static final long serialVersionUID = -2680540247105807895L;
            };

            val spi = new TrustManagerFactorySpi() {
                @Override
                protected void engineInit(final KeyStore keyStore) {
                }

                @Override
                protected void engineInit(final ManagerFactoryParameters managerFactoryParameters) {
                }

                @Override
                protected TrustManager[] engineGetTrustManagers() {
                    return getTrustManagers();
                }
            };
            return new TrustManagerFactory(spi, provider, StringUtils.EMPTY) {
            };
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
