package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.metadata.CacheCredentialsCipherExecutor;
import org.apereo.cas.authentication.metadata.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.metadata.SuccessfulHandlerMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.clearpass.ClearpassProperties;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreAuthenticationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationMetadataConfiguration implements AuthenticationEventExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "successfulHandlerMetaDataPopulator")
    @Bean
    public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
        return new SuccessfulHandlerMetaDataPopulator();
    }

    @ConditionalOnMissingBean(name = "rememberMeAuthenticationMetaDataPopulator")
    @Bean
    public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator() {
        return new RememberMeAuthenticationMetaDataPopulator();
    }

    @ConditionalOnMissingBean(name = "cacheCredentialsCipherExecutor")
    @Bean
    public CipherExecutor cacheCredentialsCipherExecutor() {
        final ClearpassProperties cp = casProperties.getClearpass();
        if (cp.isCipherEnabled() && cp.isCacheCredential()) {
            return new CacheCredentialsCipherExecutor(cp.getEncryptionKey(), cp.getSigningKey());
        }
        return NoOpCipherExecutor.getInstance();
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerMetadataPopulator(successfulHandlerMetaDataPopulator());
        plan.registerMetadataPopulator(rememberMeAuthenticationMetaDataPopulator());

        final ClearpassProperties cp = casProperties.getClearpass();
        if (cp.isCacheCredential()) {
            plan.registerMetadataPopulator(new CacheCredentialsMetaDataPopulator(cacheCredentialsCipherExecutor()));
        }
    }
}
