package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.rest.DuoSecurityRestHttpRequestCredentialFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DuoSecurityRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
@ConditionalOnClass(RestHttpRequestCredentialFactoryConfigurer.class)
@Configuration(value = "DuoSecurityRestConfiguration", proxyBeanMethods = false)
class DuoSecurityRestConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "duoSecurityRestHttpRequestCredentialFactoryConfigurer")
    public RestHttpRequestCredentialFactoryConfigurer duoSecurityRestHttpRequestCredentialFactoryConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("duoSecurityRestHttpRequestCredentialFactory")
        final RestHttpRequestCredentialFactory duoSecurityRestHttpRequestCredentialFactory) {
        return BeanSupplier.of(RestHttpRequestCredentialFactoryConfigurer.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> factory -> factory.registerCredentialFactory(duoSecurityRestHttpRequestCredentialFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityRestHttpRequestCredentialFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RestHttpRequestCredentialFactory duoSecurityRestHttpRequestCredentialFactory(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(RestHttpRequestCredentialFactory.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DuoSecurityRestHttpRequestCredentialFactory(tenantExtractor))
            .otherwiseProxy()
            .get();
    }
}
