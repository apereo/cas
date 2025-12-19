package org.apereo.cas.config;

import module java.base;
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
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * This is {@link CasCoreHttpConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@Configuration(value = "CasCoreHttpConfiguration", proxyBeanMethods = false)
class CasCoreHttpConfiguration {

    @Configuration(value = "CasCoreHttpSslFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreHttpSslFactoryConfiguration {
        @ConditionalOnMissingBean(name = "trustStoreSslSocketFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LayeredConnectionSocketFactory trustStoreSslSocketFactory(
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext,
            @Qualifier("hostnameVerifier") final HostnameVerifier hostnameVerifier) {
            return new SSLConnectionSocketFactory(casSslContext.getSslContext(), hostnameVerifier);
        }
    }

    @Configuration(value = "CasCoreHttpHostnameConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreHttpHostnameConfiguration {
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
    static class CasCoreHttpTlsConfiguration {
        @ConditionalOnMissingBean(name = CasSSLContext.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasSSLContext casSslContext(
            @Qualifier("hostnameVerifier") final HostnameVerifier hostnameVerifier,
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
    static class CasCoreHttpClientConfiguration {
        private static SimpleHttpClientFactoryBean buildHttpClientFactoryBean(
            final CasSSLContext casSslContext,
            final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) {
            val client = new SimpleHttpClientFactoryBean.DefaultHttpClient();

            val httpClient = casProperties.getHttpClient();
            client.setConnectionTimeout(Beans.newDuration(httpClient.getConnectionTimeout()).toMillis());
            client.setSocketTimeout(Beans.newDuration(httpClient.getSocketTimeout()).toMillis());
            client.setResponseTimeout(Beans.newDuration(httpClient.getResponseTimeout()).toMillis());

            if (StringUtils.isNotBlank(httpClient.getProxyHost()) && httpClient.getProxyPort() > 0) {
                client.setProxy(new HttpHost(httpClient.getProxyHost(), httpClient.getProxyPort()));
            }
            client.setSslContext(casSslContext.getSslContext());
            client.setSslSocketFactory(trustStoreSslSocketFactory);
            client.setTrustManagers(casSslContext.getTrustManagers());

            val defaultHeaders = new ArrayList<Header>();
            httpClient.getDefaultHeaders().forEach((name, value) -> defaultHeaders.add(new BasicHeader(name, value)));
            client.setDefaultHeaders(defaultHeaders);

            return client;
        }

        private static SimpleHttpClient getHttpClient(final boolean redirectEnabled,
                                                      final CasSSLContext casSslContext,
                                                      final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
                                                      final CasConfigurationProperties casProperties) {
            val factoryBean = buildHttpClientFactoryBean(casSslContext, trustStoreSslSocketFactory, casProperties);
            factoryBean.setRedirectsEnabled(redirectEnabled);
            factoryBean.setCircularRedirectsAllowed(redirectEnabled);
            return factoryBean.getObject();
        }

        @ConditionalOnMissingBean(name = HttpClient.BEAN_NAME_HTTPCLIENT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<@NonNull SimpleHttpClient> httpClient(
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext,
            @Qualifier("trustStoreSslSocketFactory") final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) {
            return buildHttpClientFactoryBean(casSslContext, trustStoreSslSocketFactory, casProperties);
        }

        @ConditionalOnMissingBean(name = HttpClient.BEAN_NAME_HTTPCLIENT_NO_REDIRECT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HttpClient noRedirectHttpClient(
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext,
            @Qualifier("trustStoreSslSocketFactory") final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) {
            return getHttpClient(false, casSslContext, trustStoreSslSocketFactory, casProperties);
        }

        @ConditionalOnMissingBean(name = HttpClient.BEAN_NAME_HTTPCLIENT_TRUST_STORE)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HttpClient supportsTrustStoreSslSocketFactoryHttpClient(
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext,
            @Qualifier("trustStoreSslSocketFactory") final LayeredConnectionSocketFactory trustStoreSslSocketFactory,
            final CasConfigurationProperties casProperties) {
            return getHttpClient(true, casSslContext, trustStoreSslSocketFactory, casProperties);
        }
    }
}
