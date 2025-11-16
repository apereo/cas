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
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication)
@Configuration(value = "CasCoreAuthenticationMetadataConfiguration", proxyBeanMethods = false)
class CasCoreAuthenticationMetadataConfiguration {
    private static final BeanCondition CONDITION_CLEARPASS = BeanCondition.on("cas.clearpass.cache-credential").isTrue();

    @Configuration(value = "CasCoreAuthenticationMetadataCipherConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationMetadataCipherConfiguration {
        @ConditionalOnMissingBean(name = "cacheCredentialsCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CipherExecutor cacheCredentialsCipherExecutor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) throws Exception {
            return BeanSupplier.of(CipherExecutor.class)
                .when(CONDITION_CLEARPASS.given(applicationContext.getEnvironment()))
                .and(BeanCondition.on("cas.clearpass.crypto.enabled").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    LOGGER.warn("CAS is configured to capture and cache credentials via Clearpass yet crypto operations for the cached password are "
                                + "turned off. Consider enabling the crypto configuration in CAS settings "
                                + "that allow the system to sign & encrypt the captured credential.");
                    return CipherExecutorUtils.newStringCipherExecutor(casProperties.getClearpass().getCrypto(), CacheCredentialsCipherExecutor.class);
                })
                .otherwise(CipherExecutor::noOp)
                .get();
        }
    }

    @Configuration(value = "CasCoreAuthenticationMetadataPopulatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationMetadataPopulatorConfiguration {
        @ConditionalOnMissingBean(name = "authenticationCredentialTypeMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator authenticationCredentialTypeMetaDataPopulator() {
            return new AuthenticationCredentialTypeMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "credentialCustomFieldsAttributeMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator credentialCustomFieldsAttributeMetaDataPopulator() {
            return new CredentialCustomFieldsAttributeMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "authenticationDateMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator authenticationDateMetaDataPopulator() {
            return new AuthenticationDateAttributeMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "clientInfoAuthenticationMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator clientInfoAuthenticationMetaDataPopulator() {
            return new ClientInfoAuthenticationMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "successfulHandlerMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
            return new SuccessfulHandlerMetaDataPopulator();
        }

        @ConditionalOnMissingBean(name = "rememberMeAuthenticationMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties) {
            return new RememberMeAuthenticationMetaDataPopulator(casProperties.getTicket().getTgt().getRememberMe());
        }
    }

    @Configuration(value = "CasCoreAuthenticationMetadataClearPassConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationMetadataClearPassConfiguration {
        @ConditionalOnMissingBean(name = "cacheCredentialsMetaDataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator cacheCredentialsMetaDataPopulator(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("cacheCredentialsCipherExecutor") final CipherExecutor cacheCredentialsCipherExecutor) throws Exception {
            return BeanSupplier.of(AuthenticationMetaDataPopulator.class)
                .when(CONDITION_CLEARPASS.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    LOGGER.warn("CAS is configured to capture and cache credentials via Clearpass. Sharing the user credential with other applications "
                                + "is generally NOT recommended, may lead to security vulnerabilities and MUST only be used as a last resort.");
                    return new CacheCredentialsMetaDataPopulator(cacheCredentialsCipherExecutor);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasCoreAuthenticationMetadataExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuthenticationMetadataExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "casCoreAuthenticationMetadataAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer casCoreAuthenticationMetadataAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("authenticationCredentialTypeMetaDataPopulator") final AuthenticationMetaDataPopulator authenticationCredentialTypeMetaDataPopulator,
            @Qualifier("credentialCustomFieldsAttributeMetaDataPopulator") final AuthenticationMetaDataPopulator credentialCustomFieldsAttributeMetaDataPopulator,
            @Qualifier("authenticationDateMetaDataPopulator") final AuthenticationMetaDataPopulator authenticationDateMetaDataPopulator,
            @Qualifier("clientInfoAuthenticationMetaDataPopulator") final AuthenticationMetaDataPopulator clientInfoAuthenticationMetaDataPopulator,
            @Qualifier("rememberMeAuthenticationMetaDataPopulator") final AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator,
            @Qualifier("successfulHandlerMetaDataPopulator") final AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator,
            @Qualifier("cacheCredentialsMetaDataPopulator") final ObjectProvider<@NonNull AuthenticationMetaDataPopulator> cacheCredentialsMetaDataPopulator) {
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
