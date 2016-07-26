package org.apereo.cas.config;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.WebflowConversationStateCipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ApplicationContextProvider;
import org.apereo.cas.util.SpringAwareMessageMessageInterpolator;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.MessageInterpolator;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreUtilConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreUtilConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreUtilConfiguration.class);
    
    @Autowired
    @Qualifier("trustStoreSslSocketFactory")
    private SSLConnectionSocketFactory trustStoreSslSocketFactory;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public CipherExecutor<byte[], byte[]> webflowCipherExecutor() {
        return new WebflowConversationStateCipherExecutor(
                casProperties.getWebflow().getEncryption().getKey(),
                casProperties.getWebflow().getSigning().getKey(),
                casProperties.getWebflow().getAlg(),
                casProperties.getWebflow().getSigning().getKeySize(),
                casProperties.getWebflow().getEncryption().getKeySize());
    }

    @Bean
    public SimpleHttpClientFactoryBean.DefaultHttpClient httpClient() {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c =
                new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        return c;
    }

    @Bean
    public HttpClient noRedirectHttpClient() throws Exception {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c =
                new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        c.setRedirectsEnabled(false);
        c.setCircularRedirectsAllowed(false);
        c.setSslSocketFactory(this.trustStoreSslSocketFactory);
        return c.getObject();
    }

    @Bean
    public HttpClient supportsTrustStoreSslSocketFactoryHttpClient() throws Exception {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c = new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        c.setSslSocketFactory(this.trustStoreSslSocketFactory);
        return c.getObject();
    }

    @Bean
    public ApplicationContextAware applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public static MessageInterpolator messageInterpolator() {
        return new SpringAwareMessageMessageInterpolator();
    }
}
