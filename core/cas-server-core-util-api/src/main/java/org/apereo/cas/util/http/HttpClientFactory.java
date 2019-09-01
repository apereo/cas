package org.apereo.cas.util.http;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * Define the factory that creates an HTTP client.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface HttpClientFactory extends FactoryBean, DisposableBean {
    HttpHost getProxy();
}
