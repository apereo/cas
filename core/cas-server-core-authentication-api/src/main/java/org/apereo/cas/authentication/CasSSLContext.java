package org.apereo.cas.authentication;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.jooq.lambda.Unchecked;
import java.security.cert.X509Certificate;

/**
 * This is {@link CasSSLContext}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface CasSSLContext {
    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "casSslContext";

    /**
     * System default ssl context, key and trust managers.
     *
     * @return the cas ssl context
     */
    static CasSSLContext system() {
        return new SystemCasSSLContext();
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
    default KeyManager[] getKeyManagers() {
        return Unchecked.supplier(() -> {
            val factory = getKeyManagerFactory();
            return factory.getKeyManagers();
        }).get();
    }

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

    /**
     * Gets key manager factory.
     *
     * @return the key manager factory
     */
    default KeyManagerFactory getKeyManagerFactory() {
        return Unchecked.supplier(() -> {
            val factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            factory.init(null, null);
            return factory;
        }).get();
    }

    class DisabledCasSslContext implements CasSSLContext {
        private static final X509Certificate[] ACCEPTED_ISSUERS = {};

        private static X509TrustManager getDisabledTrustedManager() {
            return new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return ACCEPTED_ISSUERS;
                }
            };
        }

        @Override
        public SSLContext getSslContext() {
            return Unchecked.supplier(() -> {
                val sc = SSLContext.getInstance("SSL");
                sc.init(getKeyManagers(), getTrustManagers(), null);
                return sc;
            }).get();
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

        @Override
        public TrustManagerFactory getTrustManagerFactory() {
            val provider = new Provider(StringUtils.EMPTY, "0.0", StringUtils.EMPTY) {
                @Serial
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

    }


    class SystemCasSSLContext implements CasSSLContext {
        @Override
        public SSLContext getSslContext() {
            return SSLContexts.createSystemDefault();
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
        public TrustManagerFactory getTrustManagerFactory() {
            return Unchecked.supplier(() -> {
                val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init((KeyStore) null);
                return factory;
            }).get();
        }
    }

}
