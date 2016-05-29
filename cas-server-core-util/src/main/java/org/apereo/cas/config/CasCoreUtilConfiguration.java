package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.DefaultTicketCipherExecutor;
import org.apereo.cas.WebflowCipherExecutor;
import org.apereo.cas.util.ApplicationContextProvider;
import org.apereo.cas.util.CasSpringBeanJobFactory;
import org.apereo.cas.util.NoOpCipherExecutor;
import org.apereo.cas.util.SpringAwareMessageMessageInterpolator;
import org.apereo.cas.util.TGCCipherExecutor;
import org.apereo.cas.util.http.SimpleHttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
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
public class CasCoreUtilConfiguration {

    @Value("${ticket.encryption.secretkey:}")
    private String encryptionSecretKey;
    
    @Value("${ticket.signing.secretkey:}")
    private String signingSecretKey;
    
    @Value("${ticket.secretkey.alg:AES}")
    private String secretKeyAlg;
    
    @Value("${ticket.signing.key.size:512}")
    private int signingKeySize;
    
    @Value("${ticket.encryption.key.size:16}")
    private int encryptionKeySize;


    @Value("${webflow.encryption.key:}")
    private String secretKeyEncryptionWebflow;
    
    @Value("${webflow.signing.key:}")
    private String secretKeySigningWebflow;
    
    @Value("${webflow.secretkey.alg:AES}")
    private String secretKeyAlgWebflow;
    
    @Value("${webflow.signing.key.size:512}")
    private int signingKeySizeWebflow;
    
    @Value("${webflow.encryption.key.size:16}")
    private int encryptionKeySizeWebflow;


    @Value("${tgc.encryption.key:}")
    private String secretKeyEncryptionTgc;
    
    @Value("${tgc.signing.key:}")
    private String secretKeySigningTgc;
            
    @Bean
    public CipherExecutor<byte[], byte[]> defaultTicketCipherExecutor() {
        return new DefaultTicketCipherExecutor(this.encryptionSecretKey, this.signingSecretKey,
                this.secretKeyAlg, this.signingKeySize, this.encryptionKeySize);
    }

    @Bean
    public CipherExecutor<byte[], byte[]> webflowCipherExecutor() {
        return new WebflowCipherExecutor(this.secretKeyEncryptionWebflow, this.secretKeySigningWebflow,
                this.secretKeyAlgWebflow, this.signingKeySizeWebflow, this.encryptionKeySizeWebflow);
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
        return new TGCCipherExecutor(this.secretKeyEncryptionTgc, this.secretKeySigningTgc);
    }

    @Bean
    public SpringBeanJobFactory casSpringBeanJobFactory() {
        return new CasSpringBeanJobFactory();
    }

    @Bean
    public static MessageInterpolator messageInterpolator() {
        return new SpringAwareMessageMessageInterpolator();
    }
    
    
}
