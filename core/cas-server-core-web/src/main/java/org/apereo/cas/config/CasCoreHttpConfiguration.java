package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.DefaultCasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.ArrayList;

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
    public SSLConnectionSocketFactory trustStoreSslSocketFactory() throws Exception {
        return new SSLConnectionSocketFactory(sslContext(), hostnameVerifier());
    }

    @ConditionalOnMissingBean(name = "casSslContext")
    @Bean
    public CasSSLContext casSslContext() throws Exception {
        val client = casProperties.getHttpClient().getTruststore();
        if (client.getFile() != null && client.getFile().exists() && StringUtils.isNotBlank(client.getPsw())) {
            return new DefaultCasSSLContext(client.getFile(), client.getPsw(),
                client.getType(), casProperties.getHttpClient(), hostnameVerifier());
        }
        if (casProperties.getHttpClient().getHostNameVerifier().equalsIgnoreCase("none")) {
            return CasSSLContext.disabled();
        }
        return CasSSLContext.system();
    }

    @ConditionalOnMissingBean(name = "sslContext")
    @Bean
    public SSLContext sslContext() throws Exception {
        val casSslContext = casSslContext();
        if (casSslContext != null) {
            return casSslContext.getSslContext();
        }
        return SSLContexts.createSystemDefault();

    }

    @ConditionalOnMissingBean(name = "httpClient")
    @Bean(destroyMethod = "destroy")
    public FactoryBean<SimpleHttpClient> httpClient() throws Exception {
        return buildHttpClientFactoryBean();
    }

    @ConditionalOnMissingBean(name = "noRedirectHttpClient")
    @Bean(destroyMethod = "destroy")
    public HttpClient noRedirectHttpClient() throws Exception {
        return getHttpClient(false);
    }

    @ConditionalOnMissingBean(name = "supportsTrustStoreSslSocketFactoryHttpClient")
    @Bean(destroyMethod = "destroy")
    public HttpClient supportsTrustStoreSslSocketFactoryHttpClient() throws Exception {
        return getHttpClient(true);
    }

    @ConditionalOnMissingBean(name = "hostnameVerifier")
    @Bean
    @RefreshScope
    public HostnameVerifier hostnameVerifier() {
        if (casProperties.getHttpClient().getHostNameVerifier().equalsIgnoreCase("none")) {
            return NoopHostnameVerifier.INSTANCE;
        }
        return new DefaultHostnameVerifier();
    }

    private HttpClient getHttpClient(final boolean redirectEnabled) throws Exception {
        val c = buildHttpClientFactoryBean();
        c.setRedirectsEnabled(redirectEnabled);
        c.setCircularRedirectsAllowed(redirectEnabled);

        return c.getObject();
    }

    private SimpleHttpClientFactoryBean buildHttpClientFactoryBean() throws Exception {
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

        val defaultHeaders = new ArrayList<Header>();
        httpClient.getDefaultHeaders().forEach((name, value) -> defaultHeaders.add(new BasicHeader(name, value)));
        c.setDefaultHeaders(defaultHeaders);

        return c;
    }
}
