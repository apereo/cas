package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialParser;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.SurrogatePasswordlessAuthenticationPreProcessor;
import org.apereo.cas.web.flow.SurrogatePasswordlessRequestParser;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SurrogateAuthenticationPasswordlessConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.PasswordlessAuthn,
    CasFeatureModule.FeatureCatalog.SurrogateAuthentication
})
@ConditionalOnClass(PasswordlessAuthenticationWebflowConfiguration.class)
@AutoConfiguration
public class SurrogateAuthenticationPasswordlessConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "surrogatePasswordlessAuthenticationPreProcessor")
    public PasswordlessAuthenticationPreProcessor surrogatePasswordlessAuthenticationPreProcessor(
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier("surrogatePrincipalBuilder") final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
        return new SurrogatePasswordlessAuthenticationPreProcessor(servicesManager, surrogatePrincipalBuilder);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordlessRequestParser passwordlessRequestParser(
        @Qualifier(SurrogateCredentialParser.BEAN_NAME) final SurrogateCredentialParser surrogateCredentialParser) {
        return new SurrogatePasswordlessRequestParser(surrogateCredentialParser);
    }
}
