package org.apereo.cas.util.http;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Define the factory that creates an HTTP client.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface HttpClientFactory extends FactoryBean, DisposableBean {
    HttpHost getProxy();

    LayeredConnectionSocketFactory getSslSocketFactory();

    HostnameVerifier getHostnameVerifier();

    long getConnectionTimeout();

    SSLContext getSslContext();
}
