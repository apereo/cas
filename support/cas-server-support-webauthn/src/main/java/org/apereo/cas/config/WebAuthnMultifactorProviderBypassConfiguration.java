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
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.webauthn.WebAuthnMultifactorBypassEvaluator;
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
 * This is {@link WebAuthnMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@Configuration(value = "WebAuthnMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
class WebAuthnMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "webAuthnBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val webauthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val currentBypassEvaluators = applicationContext.getBeansWithAnnotation(WebAuthnMultifactorBypassEvaluator.class).values();
        currentBypassEvaluators
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(MultifactorAuthenticationProviderBypassEvaluator.class::cast)
            .filter(evaluator -> !evaluator.isEmpty())
            .map(evaluator -> evaluator.belongsToMultifactorAuthenticationProvider(webauthn.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(bypass::addMultifactorAuthenticationProviderBypassEvaluator);
        return bypass;
    }

    @ConditionalOnMissingBean(name = "webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(webAuthn.getId(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "webAuthnRestMultifactorAuthenticationProviderBypass")
    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.web-authn.bypass.rest.url").given(applicationContext.getEnvironment()))
            .supply(() -> new RestMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "webAuthnGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnGroovyMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {

        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.web-authn.bypass.groovy.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> {
                val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
                val props = webAuthn.getBypass();
                return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "webAuthnHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnHttpRequestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnCredentialMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getCredentialClassType()))
            .supply(() -> new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(webAuthn.getId(), applicationContext);
    }

    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnPrincipalMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(StringUtils.isNotBlank(props.getPrincipalAttributeName()))
            .supply(() -> new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @WebAuthnMultifactorBypassEvaluator
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnAuthenticationMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        val bypassActive = StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName());
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(bypassActive)
            .supply(() -> new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId(), applicationContext))
            .otherwiseProxy()
            .get();
    }
}
