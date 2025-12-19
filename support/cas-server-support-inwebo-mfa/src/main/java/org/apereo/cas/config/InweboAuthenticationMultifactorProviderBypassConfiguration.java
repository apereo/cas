package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.inwebo.InweboMultifactorBypassEvaluator;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * The Inwebo MFA provider bypass configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "inwebo")
@Configuration(value = "InweboAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
class InweboAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "inweboBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator inweboBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val currentBypassEvaluators = applicationContext.getBeansWithAnnotation(InweboMultifactorBypassEvaluator.class).values();
        currentBypassEvaluators
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(MultifactorAuthenticationProviderBypassEvaluator.class::cast)
            .filter(evaluator -> !evaluator.isEmpty())
            .map(evaluator -> evaluator.belongsToMultifactorAuthenticationProvider(inwebo.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(bypass::addMultifactorAuthenticationProviderBypassEvaluator);
        return bypass;
    }

    @ConditionalOnMissingBean(name = "inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @InweboMultifactorBypassEvaluator
    public MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(inwebo.getId(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "inweboRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @InweboMultifactorBypassEvaluator
    public MultifactorAuthenticationProviderBypassEvaluator inweboRestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.in-webo.bypass.rest.url").given(applicationContext.getEnvironment()))
            .supply(() -> new RestMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "inweboGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @InweboMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator inweboGroovyMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {

        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.in-webo.bypass.groovy.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> {
                val inwebo = casProperties.getAuthn().getMfa().getInwebo();
                val props = inwebo.getBypass();
                return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "inweboHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @InweboMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator inweboHttpRequestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @InweboMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "inweboCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboCredentialMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getCredentialClassType()))
            .supply(() -> new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @InweboMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "inweboRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServiceMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(inwebo.getId(), applicationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @InweboMultifactorBypassEvaluator
    @ConditionalOnMissingBean(name = "inweboPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboPrincipalMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getPrincipalAttributeName()))
            .supply(() -> new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @InweboMultifactorBypassEvaluator
    @ConditionalOnMissingBean(name = "inweboAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboAuthenticationMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }
}
