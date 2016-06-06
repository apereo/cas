package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.DefaultTicketCipherExecutor;
import org.apereo.cas.WebflowCipherExecutor;
import org.apereo.cas.configuration.model.core.util.TicketProperties;
import org.apereo.cas.configuration.model.core.util.WebflowProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.util.ApplicationContextProvider;
import org.apereo.cas.util.CasSpringBeanJobFactory;
import org.apereo.cas.util.NoOpCipherExecutor;
import org.apereo.cas.util.SpringAwareMessageMessageInterpolator;
import org.apereo.cas.util.TGCCipherExecutor;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.validation.MessageInterpolator;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreUtilConfiguration")
@EnableConfigurationProperties({TicketProperties.class, WebflowProperties.class, TicketGrantingCookieProperties.class})
public class CasCoreUtilConfiguration {

    @Autowired
    TicketProperties ticketProperties;

    @Autowired
    WebflowProperties webflowProperties;

    @Autowired
    TicketGrantingCookieProperties tgcProperties;
            
    @Bean
    public CipherExecutor<byte[], byte[]> defaultTicketCipherExecutor() {
        return new DefaultTicketCipherExecutor(
                this.ticketProperties.getEncryption().getKey(),
                this.ticketProperties.getSigning().getKey(),
                this.ticketProperties.getSecretkey().getAlg(),
                this.ticketProperties.getSigning().getKeySize(),
                this.ticketProperties.getEncryption().getKeySize());
    }

    @Bean
    public CipherExecutor<byte[], byte[]> webflowCipherExecutor() {
        return new WebflowCipherExecutor(
                this.webflowProperties.getEncryption().getKey(),
                this.webflowProperties.getSigning().getKey(),
                this.webflowProperties.getSecretkey().getAlg(),
                this.webflowProperties.getSigning().getKeySize(),
                this.webflowProperties.getEncryption().getKeySize());
    }
    
    @Bean
    public FactoryBean<SimpleHttpClient> httpClient() {
        return new SimpleHttpClientFactoryBean.DefaultHttpClient();
    }


    @Bean
    public FactoryBean<SimpleHttpClient> noRedirectHttpClient() {
        return new SimpleHttpClientFactoryBean.NoRedirectHttpClient();
    }
    
    @Bean
    public FactoryBean<SimpleHttpClient> supportsTrustStoreSslSocketFactoryHttpClient() {
        return new SimpleHttpClientFactoryBean.SslTrustStoreAwareHttpClient();
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
        return new TGCCipherExecutor(this.tgcProperties.getEncryptionKey(), this.tgcProperties.getSigningKey());
    }
    
    @Bean
    public static MessageInterpolator messageInterpolator() {
        return new SpringAwareMessageMessageInterpolator();
    }
}
