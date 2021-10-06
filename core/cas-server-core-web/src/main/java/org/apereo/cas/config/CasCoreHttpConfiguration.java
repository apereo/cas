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
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.net.ssl.HostnameVerifier;
import java.util.ArrayList;

/**
 * This is {@link CasCoreHttpConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreHttpConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CasCoreHttpConfiguration {

    @Configuration(value = "CasCoreHttpSslFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreHttpSslFactoryConfiguration {
        @ConditionalOnMissingBean(name = "trustStoreSslSocketFactory")
        @Bean
        @Autowired
        public SSLConnectionSocketFactory trustStoreSslSocketFactory(
            @Qualifier("casSslContext")
            final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier) {
            return new SSLConnectionSocketFactory(casSslContext.getSslContext(), hostnameVerifier);
        }
    }

    @Configuration(value = "CasCoreHttpHostnameConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreHttpHostnameConfiguration {
        @ConditionalOnMissingBean(name = "hostnameVerifier")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public HostnameVerifier hostnameVerifier(final CasConfigurationProperties casProperties) {
            if (casProperties.getHttpClient().getHostNameVerifier().equalsIgnoreCase("none")) {
                return NoopHostnameVerifier.INSTANCE;
            }
            return new DefaultHostnameVerifier();
        }
    }
    
    @Configuration(value = "CasCoreHttpTlsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreHttpTlsConfiguration {
        @ConditionalOnMissingBean(name = "casSslContext")
        @Autowired
        @Bean
        public CasSSLContext casSslContext(
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            final CasConfigurationProperties casProperties) throws Exception {
            val client = casProperties.getHttpClient().getTruststore();
            if (client.getFile() != null && client.getFile().exists() && StringUtils.isNotBlank(client.getPsw())) {
                return new DefaultCasSSLContext(client.getFile(), client.getPsw(),
                    client.getType(), casProperties.getHttpClient(), hostnameVerifier);
            }
            if (casProperties.getHttpClient().getHostNameVerifier().equalsIgnoreCase("none")) {
                return CasSSLContext.disabled();
            }
            return CasSSLContext.system();
        }
    }

    @Configuration(value = "CasCoreHttpClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreHttpClientConfiguration {
        private static SimpleHttpClientFactoryBean buildHttpClientFactoryBean(
            final CasSSLContext casSslContext,
            final HostnameVerifier hostnameVerifier,
            final SSLConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) {
            val c = new SimpleHttpClientFactoryBean.DefaultHttpClient();

            val httpClient = casProperties.getHttpClient();
            c.setConnectionTimeout(Beans.newDuration(httpClient.getConnectionTimeout()).toMillis());
            c.setReadTimeout((int) Beans.newDuration(httpClient.getReadTimeout()).toMillis());

            if (StringUtils.isNotBlank(httpClient.getProxyHost()) && httpClient.getProxyPort() > 0) {
                c.setProxy(new HttpHost(httpClient.getProxyHost(), httpClient.getProxyPort()));
            }
            c.setSslSocketFactory(trustStoreSslSocketFactory);
            c.setHostnameVerifier(hostnameVerifier);
            c.setSslContext(casSslContext.getSslContext());
            c.setTrustManagers(casSslContext.getTrustManagers());
            val defaultHeaders = new ArrayList<Header>();
            httpClient.getDefaultHeaders().forEach((name, value) -> defaultHeaders.add(new BasicHeader(name, value)));
            c.setDefaultHeaders(defaultHeaders);

            return c;
        }

        private static HttpClient getHttpClient(final boolean redirectEnabled,
                                                final CasSSLContext casSslContext,
                                                final HostnameVerifier hostnameVerifier,
                                                final SSLConnectionSocketFactory trustStoreSslSocketFactory,
                                                final CasConfigurationProperties casProperties) throws Exception {
            val c = buildHttpClientFactoryBean(casSslContext, hostnameVerifier, trustStoreSslSocketFactory, casProperties);
            c.setRedirectsEnabled(redirectEnabled);
            c.setCircularRedirectsAllowed(redirectEnabled);
            return c.getObject();
        }

        @ConditionalOnMissingBean(name = "httpClient")
        @Bean(destroyMethod = "destroy")
        @Autowired
        public FactoryBean<SimpleHttpClient> httpClient(
            @Qualifier("casSslContext")
            final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            @Qualifier("trustStoreSslSocketFactory")
            final SSLConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) throws Exception {
            return buildHttpClientFactoryBean(casSslContext, hostnameVerifier,
                trustStoreSslSocketFactory, casProperties);
        }

        @ConditionalOnMissingBean(name = "noRedirectHttpClient")
        @Bean(destroyMethod = "destroy")
        @Autowired
        public HttpClient noRedirectHttpClient(
            @Qualifier("casSslContext")
            final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            @Qualifier("trustStoreSslSocketFactory")
            final SSLConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) throws Exception {
            return getHttpClient(false, casSslContext, hostnameVerifier,
                trustStoreSslSocketFactory, casProperties);
        }

        @ConditionalOnMissingBean(name = "supportsTrustStoreSslSocketFactoryHttpClient")
        @Bean(destroyMethod = "destroy")
        @Autowired
        public HttpClient supportsTrustStoreSslSocketFactoryHttpClient(
            @Qualifier("casSslContext")
            final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            @Qualifier("trustStoreSslSocketFactory")
            final SSLConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) throws Exception {
            return getHttpClient(true, casSslContext, hostnameVerifier,
                trustStoreSslSocketFactory, casProperties);
        }
    }
}
