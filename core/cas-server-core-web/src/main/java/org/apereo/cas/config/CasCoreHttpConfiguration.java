package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.DefaultCasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@AutoConfiguration
public class CasCoreHttpConfiguration {

    @Configuration(value = "CasCoreHttpSslFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreHttpSslFactoryConfiguration {
        @ConditionalOnMissingBean(name = "trustStoreSslSocketFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LayeredConnectionSocketFactory trustStoreSslSocketFactory(
            @Qualifier(CasSSLContext.BEAN_NAME)
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
        public HostnameVerifier hostnameVerifier(final CasConfigurationProperties casProperties) {
            if ("none".equalsIgnoreCase(casProperties.getHttpClient().getHostNameVerifier())) {
                return NoopHostnameVerifier.INSTANCE;
            }
            return new DefaultHostnameVerifier();
        }
    }

    @Configuration(value = "CasCoreHttpTlsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreHttpTlsConfiguration {
        @ConditionalOnMissingBean(name = CasSSLContext.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasSSLContext casSslContext(
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            final CasConfigurationProperties casProperties) throws Exception {
            val client = casProperties.getHttpClient().getTruststore();
            if (client.getFile() != null && client.getFile().exists() && StringUtils.isNotBlank(client.getPsw())) {
                return new DefaultCasSSLContext(client.getFile(), client.getPsw(),
                    client.getType(), casProperties.getHttpClient(), hostnameVerifier);
            }
            if ("none".equalsIgnoreCase(casProperties.getHttpClient().getHostNameVerifier())) {
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
            final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) {
            val client = new SimpleHttpClientFactoryBean.DefaultHttpClient();

            val httpClient = casProperties.getHttpClient();
            client.setConnectionTimeout(Beans.newDuration(httpClient.getConnectionTimeout()).toMillis());
            client.setReadTimeout((int) Beans.newDuration(httpClient.getReadTimeout()).toMillis());

            if (StringUtils.isNotBlank(httpClient.getProxyHost()) && httpClient.getProxyPort() > 0) {
                client.setProxy(new HttpHost(httpClient.getProxyHost(), httpClient.getProxyPort()));
            }
            client.setSslSocketFactory(trustStoreSslSocketFactory);
            client.setHostnameVerifier(hostnameVerifier);
            client.setSslContext(casSslContext.getSslContext());
            client.setTrustManagers(casSslContext.getTrustManagers());
            val defaultHeaders = new ArrayList<Header>();
            httpClient.getDefaultHeaders().forEach((name, value) -> defaultHeaders.add(new BasicHeader(name, value)));
            client.setDefaultHeaders(defaultHeaders);

            return client;
        }

        private static SimpleHttpClient getHttpClient(final boolean redirectEnabled,
                                                      final CasSSLContext casSslContext,
                                                      final HostnameVerifier hostnameVerifier,
                                                      final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
                                                      final CasConfigurationProperties casProperties) {
            val factoryBean = buildHttpClientFactoryBean(casSslContext, hostnameVerifier, trustStoreSslSocketFactory, casProperties);
            factoryBean.setRedirectsEnabled(redirectEnabled);
            factoryBean.setCircularRedirectsAllowed(redirectEnabled);
            return factoryBean.getObject();
        }

        @ConditionalOnMissingBean(name = "httpClient")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<SimpleHttpClient> httpClient(
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            @Qualifier("trustStoreSslSocketFactory")
            final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) throws Exception {
            return buildHttpClientFactoryBean(casSslContext, hostnameVerifier,
                trustStoreSslSocketFactory, casProperties);
        }

        @ConditionalOnMissingBean(name = HttpClient.BEAN_NAME_HTTPCLIENT_NO_REDIRECT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HttpClient noRedirectHttpClient(
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            @Qualifier("trustStoreSslSocketFactory")
            final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) throws Exception {
            return getHttpClient(false, casSslContext, hostnameVerifier,
                trustStoreSslSocketFactory, casProperties);
        }

        @ConditionalOnMissingBean(name = HttpClient.BEAN_NAME_HTTPCLIENT_TRUST_STORE)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HttpClient supportsTrustStoreSslSocketFactoryHttpClient(
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            @Qualifier("trustStoreSslSocketFactory")
            final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) throws Exception {
            return getHttpClient(true, casSslContext, hostnameVerifier,
                trustStoreSslSocketFactory, casProperties);
        }
    }
}
