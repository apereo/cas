package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.metadata.AuthenticationCredentialTypeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.AuthenticationDateAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.CacheCredentialsCipherExecutor;
import org.apereo.cas.authentication.metadata.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.metadata.ClientInfoAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.metadata.CredentialCustomFieldsAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.metadata.SuccessfulHandlerMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreAuthenticationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration(value = "casCoreAuthenticationMetadataConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationMetadataConfiguration {
    
    @Configuration(value = "CasCoreAuthenticationMetadataCipherConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationMetadataCipherConfiguration {
        @ConditionalOnMissingBean(name = "cacheCredentialsCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CipherExecutor cacheCredentialsCipherExecutor(final CasConfigurationProperties casProperties) {
            val cp = casProperties.getClearpass();
            if (cp.isCacheCredential()) {
                val crypto = cp.getCrypto();
                if (crypto.isEnabled()) {
                    return CipherExecutorUtils.newStringCipherExecutor(crypto, CacheCredentialsCipherExecutor.class);
                }
                LOGGER.warn("CAS is configured to capture and cache credentials via Clearpass yet crypto operations for the cached password are "
                            + "turned off. Consider enabling the crypto configuration in CAS settings "
                            + "that allow the system to sign & encrypt the captured credential.");
            }
            return CipherExecutor.noOp();
        }
    }
    @Configuration(value = "CasCoreAuthenticationMetadataPopulatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationMetadataPopulatorConfiguration {
        @ConditionalOnMissingBean(name = "authenticationCredentialTypeMetaDataPopulator")
        @Bean
        public AuthenticationMetaDataPopulator authenticationCredentialTypeMetaDataPopulator() {
            return new AuthenticationCredentialTypeMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "credentialCustomFieldsAttributeMetaDataPopulator")
        @Bean
        public AuthenticationMetaDataPopulator credentialCustomFieldsAttributeMetaDataPopulator() {
            return new CredentialCustomFieldsAttributeMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "authenticationDateMetaDataPopulator")
        @Bean
        public AuthenticationMetaDataPopulator authenticationDateMetaDataPopulator() {
            return new AuthenticationDateAttributeMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "clientInfoAuthenticationMetaDataPopulator")
        @Bean
        public AuthenticationMetaDataPopulator clientInfoAuthenticationMetaDataPopulator() {
            return new ClientInfoAuthenticationMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "successfulHandlerMetaDataPopulator")
        @Bean
        public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
            return new SuccessfulHandlerMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "rememberMeAuthenticationMetaDataPopulator")
        @Bean
        @Autowired
        public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties) {
            return new RememberMeAuthenticationMetaDataPopulator(casProperties.getTicket().getTgt().getRememberMe());
        }
    }

    @Configuration(value = "CasCoreAuthenticationMetadataClearPassConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationMetadataClearPassConfiguration {
        @ConditionalOnMissingBean(name = "cacheCredentialsMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnProperty(prefix = "cas.clearpass", name = "cache-credential", havingValue = "true")
        public AuthenticationMetaDataPopulator cacheCredentialsMetaDataPopulator(
            @Qualifier("cacheCredentialsCipherExecutor")
            final CipherExecutor cacheCredentialsCipherExecutor) {
            LOGGER.warn("CAS is configured to capture and cache credentials via Clearpass. Sharing the user credential with other applications "
                        + "is generally NOT recommended, may lead to security vulnerabilities and MUST only be used as a last resort .");
            return new CacheCredentialsMetaDataPopulator(cacheCredentialsCipherExecutor);
        }
    }

    @Configuration(value = "CasCoreAuthenticationMetadataExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationMetadataExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "casCoreAuthenticationMetadataAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @Autowired
        public AuthenticationEventExecutionPlanConfigurer casCoreAuthenticationMetadataAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("authenticationCredentialTypeMetaDataPopulator")
            final AuthenticationMetaDataPopulator authenticationCredentialTypeMetaDataPopulator,
            @Qualifier("credentialCustomFieldsAttributeMetaDataPopulator")
            final AuthenticationMetaDataPopulator credentialCustomFieldsAttributeMetaDataPopulator,
            @Qualifier("authenticationDateMetaDataPopulator")
            final AuthenticationMetaDataPopulator authenticationDateMetaDataPopulator,
            @Qualifier("clientInfoAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator clientInfoAuthenticationMetaDataPopulator,
            @Qualifier("rememberMeAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator,
            @Qualifier("successfulHandlerMetaDataPopulator")
            final AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator,
            @Qualifier("cacheCredentialsMetaDataPopulator")
            final ObjectProvider<AuthenticationMetaDataPopulator> cacheCredentialsMetaDataPopulator) {
            return plan -> {
                plan.registerAuthenticationMetadataPopulator(successfulHandlerMetaDataPopulator);
                plan.registerAuthenticationMetadataPopulator(rememberMeAuthenticationMetaDataPopulator);
                plan.registerAuthenticationMetadataPopulator(authenticationCredentialTypeMetaDataPopulator);
                plan.registerAuthenticationMetadataPopulator(authenticationDateMetaDataPopulator);
                plan.registerAuthenticationMetadataPopulator(credentialCustomFieldsAttributeMetaDataPopulator);
                plan.registerAuthenticationMetadataPopulator(clientInfoAuthenticationMetaDataPopulator);
                cacheCredentialsMetaDataPopulator.ifAvailable(plan::registerAuthenticationMetadataPopulator);
            };
        }
    }
}
