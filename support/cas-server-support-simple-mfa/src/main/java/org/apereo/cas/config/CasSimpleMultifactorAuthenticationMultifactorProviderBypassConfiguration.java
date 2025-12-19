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
import org.apereo.cas.mfa.simple.CasSimpleMultifactorBypassEvaluator;
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
 * This is {@link CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "casSimpleMultifactorBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val currentBypassEvaluators = applicationContext.getBeansWithAnnotation(CasSimpleMultifactorBypassEvaluator.class).values();
        currentBypassEvaluators
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(MultifactorAuthenticationProviderBypassEvaluator.class::cast)
            .filter(evaluator -> !evaluator.isEmpty())
            .map(evaluator -> evaluator.belongsToMultifactorAuthenticationProvider(simple.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(bypass::addMultifactorAuthenticationProviderBypassEvaluator);
        return bypass;
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {

        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.simple.bypass.rest.url").given(applicationContext.getEnvironment()))
            .supply(() -> {
                val simple = casProperties.getAuthn().getMfa().getSimple();
                val props = simple.getBypass();
                return new RestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.simple.bypass.groovy.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> {
                val simple = casProperties.getAuthn().getMfa().getSimple();
                val props = simple.getBypass();
                return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(simple.getId(), applicationContext);
    }

    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();

        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getCredentialClassType()))
            .supply(() -> new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(simple.getId(), applicationContext);
    }

    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @ConditionalOnMissingBean(name = "casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getPrincipalAttributeName()))
            .supply(() -> new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @CasSimpleMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

}
