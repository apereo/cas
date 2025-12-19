package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.adaptors.radius.authentication.RadiusMultifactorBypassEvaluator;
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
 * This is {@link RadiusTokenAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.RadiusMFA)
@Configuration(value = "RadiusTokenAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
class RadiusTokenAuthenticationMultifactorProviderBypassConfiguration {
    @ConditionalOnMissingBean(name = "radiusBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator radiusBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val currentBypassEvaluators = applicationContext.getBeansWithAnnotation(RadiusMultifactorBypassEvaluator.class).values();
        currentBypassEvaluators
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(MultifactorAuthenticationProviderBypassEvaluator.class::cast)
            .filter(evaluator -> !evaluator.isEmpty())
            .map(evaluator -> evaluator.belongsToMultifactorAuthenticationProvider(radius.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(bypass::addMultifactorAuthenticationProviderBypassEvaluator);
        return bypass;
    }

    @ConditionalOnMissingBean(name = "radiusRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator radiusRestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.radius.bypass.rest.url").given(applicationContext.getEnvironment()))
            .supply(() -> new RestMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "radiusGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator radiusGroovyMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {

        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.radius.bypass.groovy.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> {
                val radius = casProperties.getAuthn().getMfa().getRadius();
                val props = radius.getBypass();
                return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "radiusRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator radiusRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(radius.getId(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "radiusHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator radiusHttpRequestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "radiusCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusCredentialMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getCredentialClassType()))
            .supply(() -> new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "radiusRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusRegisteredServiceMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(radius.getId(), applicationContext);
    }

    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "radiusPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusPrincipalMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();

        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getPrincipalAttributeName()))
            .supply(() -> new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RadiusMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "radiusAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusAuthenticationMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }
}
