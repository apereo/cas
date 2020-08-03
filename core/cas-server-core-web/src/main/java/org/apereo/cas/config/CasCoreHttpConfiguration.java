package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultCasSslContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;

/**
 * This is {@link CasCoreHttpConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreHttpConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CasCoreHttpConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "trustStoreSslSocketFactory")
    @Bean
    public SSLConnectionSocketFactory trustStoreSslSocketFactory() {
        return new SSLConnectionSocketFactory(sslContext(), hostnameVerifier());
    }

    @ConditionalOnMissingBean(name = "sslContext")
    @Bean
    @SneakyThrows
    public SSLContext sslContext() {
        val client = casProperties.getHttpClient().getTruststore();
        if (client.getFile() != null && client.getFile().exists() && StringUtils.isNotBlank(client.getPsw())) {
            val ctx = new DefaultCasSslContext(client.getFile(), client.getPsw(), KeyStore.getDefaultType());
            return ctx.getSslContext();
        }
        return SSLContexts.createSystemDefault();

    }

    @ConditionalOnMissingBean(name = "httpClient")
    @Bean(destroyMethod = "destroy")
    public FactoryBean<SimpleHttpClient> httpClient() {
        return buildHttpClientFactoryBean();
    }

    @ConditionalOnMissingBean(name = "noRedirectHttpClient")
    @Bean(destroyMethod = "destroy")
    public HttpClient noRedirectHttpClient() {
        return getHttpClient(false);
    }

    @ConditionalOnMissingBean(name = "supportsTrustStoreSslSocketFactoryHttpClient")
    @Bean(destroyMethod = "destroy")
    public HttpClient supportsTrustStoreSslSocketFactoryHttpClient() {
        return getHttpClient(true);
    }

    @ConditionalOnMissingBean(name = "hostnameVerifier")
    @Bean
    public HostnameVerifier hostnameVerifier() {
        if (casProperties.getHttpClient().getHostNameVerifier().equalsIgnoreCase("none")) {
            return NoopHostnameVerifier.INSTANCE;
        }
        return new DefaultHostnameVerifier();
    }

    private HttpClient getHttpClient(final boolean redirectEnabled) {
        val c = buildHttpClientFactoryBean();
        c.setRedirectsEnabled(redirectEnabled);
        c.setCircularRedirectsAllowed(redirectEnabled);

        return c.getObject();
    }

    private SimpleHttpClientFactoryBean buildHttpClientFactoryBean() {
        val c = new SimpleHttpClientFactoryBean.DefaultHttpClient();

        val httpClient = casProperties.getHttpClient();
        c.setConnectionTimeout(Beans.newDuration(httpClient.getConnectionTimeout()).toMillis());
        c.setReadTimeout((int) Beans.newDuration(httpClient.getReadTimeout()).toMillis());

        if (StringUtils.isNotBlank(httpClient.getProxyHost()) && httpClient.getProxyPort() > 0) {
            c.setProxy(new HttpHost(httpClient.getProxyHost(), httpClient.getProxyPort()));
        }
        c.setSslSocketFactory(trustStoreSslSocketFactory());
        c.setHostnameVerifier(hostnameVerifier());
        c.setSslContext(sslContext());

        return c;
    }
}
