package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyBypassEvaluator;
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
 * This is {@link YubiKeyAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.YubiKey)
@Configuration(value = "YubiKeyAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
class YubiKeyAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "yubikeyBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val currentBypassEvaluators = applicationContext.getBeansWithAnnotation(YubiKeyBypassEvaluator.class).values();
        currentBypassEvaluators
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(MultifactorAuthenticationProviderBypassEvaluator.class::cast)
            .filter(evaluator -> !evaluator.isEmpty())
            .map(evaluator -> evaluator.belongsToMultifactorAuthenticationProvider(yubikey.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(bypass::addMultifactorAuthenticationProviderBypassEvaluator);
        return bypass;
    }

    @ConditionalOnMissingBean(name = "yubikeyRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @YubiKeyBypassEvaluator
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyRestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.yubikey.bypass.rest.url").given(applicationContext.getEnvironment()))
            .supply(() -> new RestMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "yubikeyGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @YubiKeyBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyGroovyMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.yubikey.bypass.groovy.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> {
                val yubikey = casProperties.getAuthn().getMfa().getYubikey();
                val props = yubikey.getBypass();
                return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "yubikeyHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @YubiKeyBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyHttpRequestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "yubikeyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @YubiKeyBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(yubikey.getId(), applicationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @YubiKeyBypassEvaluator
    @ConditionalOnMissingBean(name = "yubikeyCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyCredentialMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getCredentialClassType()))
            .supply(() -> new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @YubiKeyBypassEvaluator
    @ConditionalOnMissingBean(name = "yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(yubikey.getId(), applicationContext);
    }

    @Bean
    @YubiKeyBypassEvaluator
    @ConditionalOnMissingBean(name = "yubikeyPrincipalMultifactorAuthenticationProviderBypass")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyPrincipalMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getPrincipalAttributeName()))
            .supply(() -> new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @YubiKeyBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyAuthenticationMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }
}
