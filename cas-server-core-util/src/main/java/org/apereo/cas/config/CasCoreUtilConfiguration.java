package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.DefaultTicketCipherExecutor;
import org.apereo.cas.WebflowCipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ApplicationContextProvider;
import org.apereo.cas.util.NoOpCipherExecutor;
import org.apereo.cas.util.SpringAwareMessageMessageInterpolator;
import org.apereo.cas.util.TGCCipherExecutor;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CasCoreUtilConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public CipherExecutor<byte[], byte[]> defaultTicketCipherExecutor() {
        return new DefaultTicketCipherExecutor(
                casProperties.getTicket().getEncryption().getKey(),
                casProperties.getTicket().getSigning().getKey(),
                casProperties.getTicket().getSecretkey().getAlg(),
                casProperties.getTicket().getSigning().getKeySize(),
                casProperties.getTicket().getEncryption().getKeySize());
    }

    @Bean
    public CipherExecutor<byte[], byte[]> webflowCipherExecutor() {
        return new WebflowCipherExecutor(
                casProperties.getWebflow().getEncryption().getKey(),
                casProperties.getWebflow().getSigning().getKey(),
                casProperties.getWebflow().getSecretkey().getAlg(),
                casProperties.getWebflow().getSigning().getKeySize(),
                casProperties.getWebflow().getEncryption().getKeySize());
    }

    @Bean
    public FactoryBean<SimpleHttpClient> httpClient() {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c =
                new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        return c;
    }


    @Bean
    public FactoryBean<SimpleHttpClient> noRedirectHttpClient() {
        final SimpleHttpClientFactoryBean.NoRedirectHttpClient c =
                new SimpleHttpClientFactoryBean.NoRedirectHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        return c;
    }

    @Bean
    public FactoryBean<SimpleHttpClient> supportsTrustStoreSslSocketFactoryHttpClient() {
        final SimpleHttpClientFactoryBean.SslTrustStoreAwareHttpClient c =
                new SimpleHttpClientFactoryBean.SslTrustStoreAwareHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        return c;
    }

    @Bean
    public ApplicationContextAware applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public CipherExecutor noOpCipherExecutor() {
        return new NoOpCipherExecutor();
    }


    @Bean
    public CipherExecutor tgcCipherExecutor() {
        return new TGCCipherExecutor(casProperties.getTgc().getEncryptionKey(),
                casProperties.getTgc().getSigningKey());
    }

    @Bean
    public static MessageInterpolator messageInterpolator() {
        return new SpringAwareMessageMessageInterpolator();
    }
}
