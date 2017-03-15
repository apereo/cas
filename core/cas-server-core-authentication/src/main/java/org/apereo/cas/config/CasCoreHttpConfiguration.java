package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apereo.cas.authentication.FileTrustStoreSslSocketFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.net.ssl.HostnameVerifier;

/**
 * This is {@link CasCoreHttpConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreHttpConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureBefore(CasCoreAuthenticationConfiguration.class)
public class CasCoreHttpConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "trustStoreSslSocketFactory")
    @Bean
    public SSLConnectionSocketFactory trustStoreSslSocketFactory() {
        final HttpClientProperties.Truststore client = casProperties.getHttpClient().getTruststore();
        if (client.getFile() != null && client.getFile().exists() && StringUtils.isNotBlank(client.getPsw())) {
            return new FileTrustStoreSslSocketFactory(client.getFile(), client.getPsw());
        }
        return new SSLConnectionSocketFactory(SSLContexts.createSystemDefault());
    }

    @ConditionalOnMissingBean(name = "httpClient")
    @Bean
    public FactoryBean<SimpleHttpClient> httpClient() {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c = new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(Long.valueOf(casProperties.getHttpClient().getReadTimeout()).intValue());
        return c;
    }

    @ConditionalOnMissingBean(name = "noRedirectHttpClient")
    @Bean
    public HttpClient noRedirectHttpClient() throws Exception {
        return getHttpClient(false);
    }

    @ConditionalOnMissingBean(name = "supportsTrustStoreSslSocketFactoryHttpClient")
    @Bean
    public HttpClient supportsTrustStoreSslSocketFactoryHttpClient() throws Exception {
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

    private HttpClient getHttpClient(final boolean redirectEnabled) throws Exception {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c = new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(Long.valueOf(casProperties.getHttpClient().getReadTimeout()).intValue());
        c.setRedirectsEnabled(redirectEnabled);
        c.setCircularRedirectsAllowed(redirectEnabled);
        c.setSslSocketFactory(trustStoreSslSocketFactory());
        c.setHostnameVerifier(hostnameVerifier());
        return c.getObject();
    }
}
