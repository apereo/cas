package org.apereo.cas.util.http;

import module java.base;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * Define the factory that creates an HTTP client.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface HttpClientFactory extends FactoryBean, DisposableBean {
    /**
     * Gets proxy.
     *
     * @return the proxy
     */
    HttpHost getProxy();

    /**
     * Gets ssl socket factory.
     *
     * @return the ssl socket factory
     */
    LayeredConnectionSocketFactory getSslSocketFactory();

    /**
     * Gets hostname verifier.
     *
     * @return the hostname verifier
     */
    HostnameVerifier getHostnameVerifier();

    /**
     * Gets connection timeout.
     *
     * @return the connection timeout
     */
    long getConnectionTimeout();

    /**
     * Gets ssl context.
     *
     * @return the ssl context
     */
    SSLContext getSslContext();

    /**
     * Get trust managers trust managers [].
     *
     * @return the trust manager []
     */
    TrustManager[] getTrustManagers();
}
